package com.microshop.users.application.query;

import com.microshop.users.infrastructure.persistence.entity.UserCompanyEntity;
import com.microshop.users.infrastructure.persistence.repository.UserCompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserCompanyQueryService {

    private final UserCompanyRepository userCompanyRepository;

    public List<UserCompanyEntity> getUserCompanies(@NonNull Long userId) {
        return userCompanyRepository.findByUsuarioId(userId);
    }

    /**
     * ¿El usuario indicado pertenece al tenant indicado? Es la pregunta que los endpoints por
     * {@code {id}} de usuario tienen que hacerse antes de responder: como {@code UsuarioEntity} no
     * lleva {@code company_id}, sin esta comprobación un ADMIN de cualquier empresa podía leer,
     * modificar o borrar usuarios de otra con solo cambiar el id de la URL.
     */
    public boolean perteneceAlTenant(@NonNull Long usuarioId, @NonNull Long companyId) {
        return userCompanyRepository.existsByUsuarioIdAndCompanyId(usuarioId, companyId);
    }

    /**
     * Ids de los usuarios con rol de administración (ADMIN o SUPERADMIN) que son miembros ACTIVOS de
     * una empresa. Sirve para que otro servicio pueda dirigir una notificación operativa <b>a esa
     * empresa</b> en lugar de a un destinatario adivinado.
     *
     * <p>Existe por un fallo concreto: el evaluador de alertas de stock de logística iteraba todos los
     * tenants y enviaba el inventario crítico de cada uno a un {@code userId} HARDCODEADO, que sólo era
     * miembro de dos de las seis empresas — entregaba a un usuario datos de empresas ajenas, en un
     * {@code @Scheduled} que ningún escáner de endpoints HTTP puede ver.</p>
     *
     * <p>Va en el QueryService y no en el controller por dos razones: la convención del proyecto
     * (los controllers no inyectan repositorios) y, sobre todo, porque navegar
     * {@code usuario.rol.nombre} exige una transacción abierta — hacerlo en el controller producía
     * {@code LazyInitializationException} y el endpoint devolvía 500 justo para las empresas que SÍ
     * tienen administradores. El {@code @EntityGraph} de {@code findByCompanyId} trae
     * {@code roles}/{@code roles.rol}, no {@code usuario.rol}, así que la carga de esa relación
     * ocurre aquí, dentro del {@code @Transactional(readOnly = true)} de la clase.</p>
     *
     * <p>Lista vacía si no hay ninguno: el consumidor debe entonces NO notificar, nunca caer a un
     * destinatario por defecto.</p>
     */
    public List<Long> adminUserIdsDeEmpresa(@NonNull Long companyId) {
        return userCompanyRepository.findByCompanyId(companyId).stream()
                .filter(UserCompanyEntity::isActive)
                .map(UserCompanyEntity::getUsuario)
                .filter(u -> u != null && u.getRol() != null && u.getRol().getNombre() != null)
                .filter(u -> esRolDeAdministracion(u.getRol().getNombre()))
                .map(u -> u.getId())
                .distinct()
                .toList();
    }

    private static boolean esRolDeAdministracion(String nombreRol) {
        String r = nombreRol.toUpperCase();
        return com.microshop.users.shared.constants.AppConstants.Seguridad.ADMIN.equals(r)
                || com.microshop.users.shared.constants.AppConstants.Seguridad.SUPERADMIN.equals(r);
    }
}
