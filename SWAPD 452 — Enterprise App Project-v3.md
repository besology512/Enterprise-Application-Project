**SWAPD 452 — Enterprise Application Development** 

**Capstone Project**  

**WorkHub: Engineering and Operating a Multi-Tenant SaaS Backend** 

**Total Marks:** 100 

**Team Size:** 3–4 students 

**Phases & Deadlines:** **Week 7 (Phase 1)**, **Week 12 (Phase 2)**, **Week 14 (Phase 3 + Individual Defense)** 

` `**Core Rule:** All teams build the same core app for fair grading. Optional extensions are allowed. 

1) **Project Goal** 

Build a production-style multi-tenant SaaS backend with enterprise concerns:

- Framework internals awareness (DI, AOP patterns, auto-config debugging mindset) 
- Transactional integrity and rollback 
- Concurrency safety (no lost updates)
- Security (JWT + RBAC) 
- Multi-tenancy with strict tenant isolation
- Messaging for async workflows 
- Observability (health, metrics, readiness/liveness, correlation logging)
- Cloud-native deployment (Docker + Kubernetes) 
- Infrastructure as Code (IaC) using Terraform 
- CI/CD running tests and building images 
- Individual understanding verified via a technical defense

Each student must defend a random code construct during discussion. 

2) **System Theme: WorkHub SaaS** 

WorkHub is a multi-tenant project management backend where each Tenant (Organization) has users and can manage projects and tasks.

**Minimum Domain (Required)** 

- **Tenant**: tenantId, name, plan 
- **User**: email, password hash (or simplified), roles, tenantId 
- **Project**: tenantId, name, createdBy 
- **Task**: tenantId, projectId, status, (optional) counter/version
- **Job/Report**: async generated output status
3) **Required API (Minimum)** 

**Auth** 

- POST /auth/login → returns JWT including tenantId claim 
- GET /auth/me → returns authenticated user + tenant

**Projects/Tasks** 

- POST /projects 
- GET /projects 
- GET /projects/{id} 
- POST /projects/{id}/tasks 
- PATCH /tasks/{id} 

**Async** 

- POST /projects/{id}/generate-report → enqueues message; later marks report/job completed 
4) **Mandatory Enterprise Requirements** 
1. **Security (JWT + RBAC)** 
- JWT required for all endpoints except /auth/login 
- Roles: TENANT\_ADMIN, TENANT\_USER 
- Admin-only operations must exist (you choose one, e.g., create project, invite user, change plan) and be enforced
2. **Multi-Tenancy (Tenant Isolation)** 

Minimum approach: Shared DB + tenant\_id column and strict filtering on every query + write. 

Important: Tenant A must never read/write/list Tenant B data.

3. **Transactions** 

At least one multi-step business operation must be transactional and must rollback fully on failure. 

4. **Concurrency Safety** 

Demonstrate protection against lost updates using one of:

- Optimistic locking ( @Version + retry) 
- Pessimistic locking (SELECT … FOR UPDATE / repository lock) 
5. **Messaging (Kafka or RabbitMQ)** 

Async workflow must be implemented via messaging (not just @Async): 

- Producer publishes an event/job message
- Consumer processes and updates DB state
- Basic reliability: at least one of: 
- idempotency key / processed-message table 
- safe retry behavior documented and tested
6. **Observability** 
- Spring Actuator enabled 
- Health endpoints available 
- Readiness and liveness endpoints configured
- Metrics available via Micrometer 
- Correlation ID in logs (request tracing at least at log level)
7. **Deployment + IaC + CI** 
- Dockerfile + docker-compose (local stack) 
- Kubernetes manifests (deployment/service/config/probes)
- Terraform IaC required (VM track or Kubernetes track) 
- CI pipeline runs tests and builds Docker image
5) **Project Phases, Deliverables, and Rubrics**

**PHASE 1 — Week 7: Foundation Release** 

**Marks: 20** 

**What you must deliver** 

1. Working Spring Boot backend with clean layering 
1. JWT login + /auth/me 
1. Tenant context extraction (filter/interceptor)
1. DTO validation + consistent error responses
1. At least one transactional write use-case + rollback demonstration

**Phase 1 Deliverables** 

- Git tag: v1-phase1-week7 
- README.md (how to run locally) 
- Postman collection or curl script (login + CRUD flows) 
- DESIGN-NOTE.pdf (2–3 pages): architecture, tenant approach, transaction boundary 

**Phase 1 Rubric (20)** 

- Architecture/layering/DTOs/error handling: **6** 
- JWT auth + /auth/me: **6** 
- Tenant context consistently applied: **4** 
- Transaction rollback demonstrated clearly: **4** 

**PHASE 2 — Week 12: SaaS Hardening** 

**Marks: 30** 

**What you must deliver** 

1. Strict tenant isolation everywhere (read/write/list)
1. RBAC enforced (401/403 behaviors correct) 
1. Messaging-based async workflow (Kafka/RabbitMQ) 
1. Observability: actuator, metrics, readiness/liveness, correlation logs

**Phase 2 Deliverables** 

- Git tag: v2-phase2-week12 
- TENANT-ISOLATION-PROOF.md (steps + expected result + evidence)
- OBSERVABILITY.md (which endpoints + how to verify) 
- Demo script (Postman/curl) showing async job creation and completion

**Phase 2 Rubric (30)** 

- Tenant isolation correctness + proof: **12** 
- RBAC enforcement correctness: **8** 
- Messaging async workflow works + basic reliability: **6** 
- Observability configured and demonstrable: **4** 

**Hard Gate:** If any tenant leak is found → **Phase 2 capped at 50%**. 

**PHASE 3 — Week 14: Cloud -Native Delivery + IaC + CI** 

**Marks: 20** 

**What you must deliver** 

1) **Containers** 
- Dockerfile for service 
- docker-compose.yml for app + Postgres + Kafka/Rabbit 
2) **Kubernetes** 
- k8s/ manifests: 
- Deployment 
- Service 
- ConfigMap 
- Secrets pattern (or Secret manifest)
- Readiness/liveness probes pointing to Actuator endpoints
3) **Infrastructure as Code (MANDATORY) — Terraform**  Choose one track: 

   **Track 1: VM Track (Cloud VM)** 

- Terraform provisions a VM + networking/firewall rules needed 
- Deployment on VM can be via Docker Compose (acceptable)

**Track 2: Kubernetes Track** 

- Terraform provisions Kubernetes resources (namespace + at least deployment/service OR cluster resources if you choose)
4) **CI/CD** 
- GitHub Actions pipeline: 
- build 
- run tests 
- build Docker image (push optional) 

**Phase 3 Deliverables** 

- Git tag: v3-phase3-week14 
- DEPLOYMENT.md (local compose + k8s + chosen free deployment option)
- terraform/ folder (main.tf, variables.tf, outputs.tf, example tfvars, README) 
- CI workflow file in .github/workflows/ 

**Phase 3 Rubric (20)** 

- Docker + Compose end-to-end works: **5** 
- Kubernetes manifests correct incl. probes/config: **5** 
- Terraform IaC completeness + reproducibility: **6** 
- CI pipeline runs build + tests + image build: **4** 

**IaC Hard Gate:** If Terraform is missing or infra is done manually → **Phase 3 max = 50%**. 

6) **Enterprise Testing (Mandatory, Automated, in CI)** 

**Marks: 15** 

**All teams must implement integration tests (not only unit tests). Tests must run in CI.**

**Required Tests + How to Test Each** 

1) **Tenant Isolation — 4 marks** 

**How:** Integration tests using MockMvc/WebTestClient

- Create data under tenant B 
- Call endpoints using tenant A JWT 
- Expect **403 or 404** (your team must document which and why)

  ` `Required: 3 tests 

1. Cross-tenant read (GET /projects/{id}) 
1. Cross-tenant update (PATCH /tasks/{id}) 
1. Cross-tenant list (GET /projects / GET /tasks) must not leak any items 
2) **RBAC — 3 marks** 

Integration tests for 401/403 and role restriction  Required: 3 tests 

1. Missing token → 401 
1. Wrong role → 403 
1. Admin allowed → 200/201 
3) **Transaction Rollback — 2 marks** 

Integration or service-level integration test with real DB 

- Perform multi-step operation that throws midway
- Assert DB contains **no partial writes** 

  ` `Required: 1 test 

4) **Concurrency — 3 marks** 

Multi-thread integration test against real DB

- Run N concurrent updates to same row
- Assert final value is correct OR conflicts handled correctly  Required: 1 test 
5) **Messaging Reliability — 2 marks** 

` `Testcontainers Kafka/Rabbit + Awaitility 

- Publish message 
- Await consumer processing 
- Assert DB state updated 

  ` `Required: 1 test to show retry/idempotency 

6) **Observability Verification — 1 mark** 

` `Integration test hits Actuator endpoints

- /actuator/health is OK 
- readiness/liveness endpoints are available  Required: 1 test 

**Enterprise Testing Rubric (15)** 

- Tenant isolation tests: **4** 
- RBAC tests: **3** 
- Transaction rollback test: **2** 
- Concurrency test: **3** 
- Messaging test: **2** 
- Observability test: **1** 

**Hard Gate:** If no integration tests → Enterprise Testing = **0**. **Hard Gate:** If tests don’t run in CI → CI score in Phase 3 = **0**. 

7) **Individual Technical Defense**  

**Marks: 15 (Individual)** 

**Each student will be asked about a random code construct from the team repository.** 

**Format** 

- 5–7 minutes per student 
- Student must explain: 
- What it does 
- Why it exists 
- What breaks if removed/changed
- Enterprise implications (security/data loss/leaks/latency/outage)

**Two-Attempt Rule** 

Each student gets two opportunities only.

` `If they fail to answer correctly → Individual Defense = 0 (no third attempt). 

**Construct Categories include, but not limited to:** 

- @Transactional boundaries/propagation and rollback
- tenant filter / tenant context / repository filters
- Spring Security filter chain / JWT validation 
- RBAC configuration 
- @Version or DB locking strategy 
- Kafka/Rabbit consumer + idempotency/retry strategy
- Testcontainers configuration 
- readiness/liveness probes setup
- Terraform resources + why IaC prevents drift 
- CI workflow steps and why tests must run
8) **Final Grade Breakdown (100)** 
- Phase 1: **20** 
- Phase 2: **30** 
- Phase 3: **20** 
- Enterprise Testing: **15** 
- Individual Technical Defense: **15** 
9) **Required Repository Structure (Minimum)** 
- README.md 
- DEPLOYMENT.md 
- TESTPLAN.md (lists all required tests with links/test names)
- TENANT-ISOLATION-PROOF.md 
- OBSERVABILITY.md 
- terraform/ (IaC) 
- .github/workflows/ci.yml 
- k8s/ (manifests) 
- docker-compose.yml 
- Dockerfile 
10) **Deployment Options**  

Minimum: local Docker Compose (required for all teams) Optional free deployment tracks (choose one):

- Render (easy demo deployment; free tier limitations may apply)
- Oracle Cloud Always Free VM (best “free” for DevOps learning; requires account verification) 
- Local Kubernetes (minikube/kind) + Terraform Kubernetes provider
11) **Bonus Extensions (+5 to +10, capped depending on maturity of implementation)** 
- schema-per-tenant isolation 
- outbox pattern for messaging reliability 
- rate limiting per tenant 
- tracing (OpenTelemetry) 
- load testing + SLO report 
- blue/green or canary deployment strategy

**What you must submit for IaC** 

Inside terraform/: 

- main.tf 
- variables.tf 
- outputs.tf 
- README.md with terraform init/plan/apply 
- Example terraform.tfvars.example (no secrets) 
