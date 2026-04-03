# Code Review Rules — microshopusers

## Java / Spring Boot

- Java 21, Spring Boot 3.x
- Use constructor injection only — never `@Autowired` on fields
- Use Lombok: `@RequiredArgsConstructor`, `@Data`, `@Builder`, `@Getter`, `@Setter`
- Use `record` for DTOs
- Custom exceptions: `NotFoundException`, `BusinessException`, `ConflictException`
- Messages externalized to `i18n/messages*.properties` (Spanish default)
- Javadoc and complex comments in Spanish; code identifiers in English
- Never use `@Autowired` on fields
- Prefer `Optional` over null returns
- Transactions: `@Transactional(readOnly = true)` for queries, `@Transactional` for writes

## Architecture

- Hexagonal architecture: controller → application (command/query) → domain → infrastructure
- CQRS: `*CommandService` for writes, `*QueryService` for reads
- JPA entities in `infrastructure/persistence/entity/`
- Repositories in `infrastructure/persistence/repository/`
- Controllers in `infrastructure/rest/controller/`
- Multi-tenancy: always filter by `company_id`

## Security

- JWT RSA-256 tokens
- `@AuthenticationPrincipal UserDetails` for authenticated endpoints
- Internal endpoints (`/api/internal/**`) are permitAll
