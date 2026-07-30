package com.microshop.users.application.command;

import com.microshop.users.infrastructure.persistence.repository.SesionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Revocación de sesiones ya emitidas (M27).
 *
 * <h2>El problema que cierra</h2>
 * <p>La tabla {@code sesion} tenía 1267 filas y las 1267 estaban marcadas válidas: nada las
 * invalidaba nunca. B08 cerró el bloqueo en el <b>login</b> —una empresa suspendida ya no deja
 * entrar—, pero un token ya emitido seguía valiendo hasta expirar, así que suspender un tenant no
 * cortaba a quien estuviera dentro. Hasta 24 h operando con normalidad después de la suspensión.</p>
 *
 * <h2>Alcance de esta clase, dicho sin adornos</h2>
 * <p>Esto revoca en la <b>base</b> y {@code microshopusers} lo hace efectivo <b>en el acto</b>, porque
 * su filtro JWT consulta la sesión. Los otros cinco servicios validan el token solo con la clave
 * pública y no consultan nada, así que <b>para ellos la revocación todavía no es efectiva</b>: se
 * enteran cuando el token expira. Cerrar eso exige que cada uno pregunte por el estado del token, y
 * para eso está {@code GET /api/internal/sesiones/{jti}/revocada}, que este servicio ya expone. No se
 * finge lo contrario: la mitad del recorrido está hecha y la otra es una adopción por servicio.</p>
 *
 * <p>Se eligió esta vía y no las otras dos con criterio. <b>Tokens cortos + refresh</b> es la solución
 * de libro y hace la revocación efectiva en todas partes sin coste por petición, pero exige un flujo
 * de refresco en el frontend y tocar los seis servicios a la vez. <b>Consultar la empresa en cada
 * petición</b> convierte cada request en una llamada entre servicios. La lista de revocación sobre el
 * {@code jti} —que existe desde M42— revoca de verdad, es barata y se puede adoptar servicio a
 * servicio sin romper nada mientras tanto.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SesionRevocacionService {

    private final SesionRepository sesionRepository;

    /**
     * Invalida todas las sesiones vivas de una empresa. Se llama al suspenderla.
     *
     * <p>{@code REQUIRES_NEW} a propósito: la revocación tiene que quedar comprometida aunque la
     * transacción que la disparó falle después. Si la suspensión se revierte por cualquier motivo, es
     * preferible haber cortado sesiones de más —el usuario vuelve a entrar— que haber suspendido una
     * empresa cuyos usuarios siguen dentro.</p>
     *
     * @return número de sesiones invalidadas
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int revocarPorEmpresa(Long companyId) {
        if (companyId == null) return 0;
        int n = sesionRepository.invalidarPorCompany(companyId);
        if (n > 0) {
            log.info("Revocadas {} sesiones vivas de la empresa {}. Efectivo de inmediato en users; "
                    + "los demas servicios lo veran cuando adopten la comprobacion de jti.", n, companyId);
        }
        return n;
    }

    /** Invalida todas las sesiones vivas de un usuario. Logout global y reseteo de contraseña. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int revocarPorUsuario(Long usuarioId) {
        if (usuarioId == null) return 0;
        int n = sesionRepository.invalidarPorUsuario(usuarioId);
        if (n > 0) {
            log.info("Revocadas {} sesiones vivas del usuario {}", n, usuarioId);
        }
        return n;
    }

    /**
     * {@code true} si el token identificado por ese {@code jti} está revocado.
     *
     * <p>Un {@code jti} <b>desconocido</b> se considera NO revocado, y merece explicación porque parece
     * lo contrario de lo seguro: las sesiones creadas antes de V40 no tienen {@code jti} guardado, así
     * que tratar «desconocido» como revocado expulsaría a todo el mundo en el despliegue. La firma del
     * token ya se validó antes de llegar aquí, o sea que un {@code jti} desconocido es un token
     * legítimo emitido por nosotros y no una falsificación. Cuando no queden sesiones sin {@code jti}
     * —24 h después del despliegue— esto puede invertirse.</p>
     */
    @Transactional(readOnly = true)
    public boolean estaRevocado(String jti) {
        if (jti == null || jti.isBlank()) return false;
        return sesionRepository.findByJti(jti)
                .map(s -> !s.isValido())
                .orElse(false);
    }
}
