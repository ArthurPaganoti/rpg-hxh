# ADR 006 — Monolito modular (functional slices) em vez de microsservicos

## Status

Aceito (2026-08-01) — decisao registrada; preparacoes listadas abaixo ainda nao implementadas.

## Contexto

Foi avaliada a migracao do rpg-hxh para arquitetura de microsservicos nos moldes do projeto CronoWise (crono-hub), com o argumento "migrar cedo e barato, pensando no futuro".

A analise do crono-hub revelou o custo real do modelo: ~82k LOC de backend Java, 8 deployables, API gateway proprio (~3k LOC so de roteamento/validacao), Kafka + Zookeeper, MinIO, Kubernetes com Istio, tokens internos entre servicos e 8 pipelines de CI separados. E o dado decisivo: **mesmo la, todos os servicos rodam com `replicas: 1`, sem autoscaling, com URLs estaticas (sem service discovery) e um unico Postgres** — a divisao dos servicos Java e preferencia estrutural, nao necessidade de escala. As unicas fronteiras genuinamente justificadas sao o worker Python de RPA (runtime diferente, desacoplado via Kafka) e o captcha-solver (integracoes externas isolaveis). Alem disso, o proprio crono-hub **foi monolito e migrou depois** (`docs/MIGRATION_PLAN.md` no repo deles), com o dominio maduro — provando que a migracao tardia e viavel.

O rpg-hxh hoje: ~6k LOC, desenvolvedor solo, pre-produto, e o nucleo do dominio (personagens, Nen, fichas, mecanicas do Hunter Legacy) ainda nao existe.

## Decisao

Manter o **monolito modular com functional slices** e nao migrar para microsservicos agora. Motivos:

1. **O custo de microsservicos nao e a migracao — e o imposto operacional perpetuo**: cada feature passa a exigir orquestrar N processos localmente, autenticacao entre servicos, debugging distribuido e multiplos pipelines. Para um dev solo, esse imposto e pago pela mesma pessoa que deveria construir o produto.
2. **Microsservico congela fronteiras, e as fronteiras do dominio ainda vao mudar**: o dominio do jogo nao existe; desenhar servicos antes de conhece-lo e apostar as fronteiras no pior momento. Mover fronteira entre pacotes custa um refactor; entre servicos custa migracao de dados, versionamento de API e deploy coordenado.
3. **Escala nao e risco no horizonte relevante**: o monolito e stateless (JWT + Redis externo) e escala primeiro verticalmente e depois por replicas atras de um load balancer, sem mudanca de arquitetura. RPG de mesa e trafego baixo por usuario.
4. **A arquitetura atual ja e a preparacao**: slices sem import cruzado sao exatamente a costura por onde se extrai um servico no futuro.

## Gatilhos objetivos de migracao (quando reavaliar este ADR)

Reavaliar a extracao de servicos quando **qualquer** um ocorrer:

- (a) Segundo desenvolvedor fixo no projeto (fronteira de time e o motivador classico de microsservicos);
- (b) SSE saturando uma instancia — conexoes simultaneas ou latencia p95 de eventos acima da meta, medidos pela observabilidade (nao por percepcao);
- (c) Um modulo exigindo ciclo de deploy independente do restante;
- (d) Escala vertical + replicas do monolito esgotadas.

## Mapa de costuras (ordem de extracao futura, se um gatilho disparar)

1. **Notificacoes/e-mail** — quando a infraestrutura de e-mail nascer (convites por e-mail, verificacao de conta), e o analogo natural do ms-notifications do crono-hub: assincrono, sem estado compartilhado com o resto;
2. **Realtime/SSE** — a costura ja desenhada no ADR 005: os POSTs de acao ficam no monolito, apenas o fan-out (Redis pub/sub → SSE) sai para um servico dedicado;
3. **Dominio do jogo** — somente apos maduro e estavel.

## Preparacoes ("monolito preparado") — a executar

| # | Peca | Racional | Custo estimado |
|---|------|----------|----------------|
| 1 | **JWT RS256 + JWKS** (`/.well-known/jwks.json`) | Validacao de token desacoplada do emissor: qualquer servico/gateway futuro valida sem segredo compartilhado (padrao ms-auth + gateway do crono-hub). A peca mais "microservices-ready" que existe. | ~1 dia |
| 2 | **Observabilidade** (Actuator + Micrometer/Prometheus; profile `observability` no docker-compose com Prometheus + Grafana) | Da os numeros que dizem se um gatilho disparou; ja constava como planejado no initial_setup. | ~meio dia |
| 3 | **CI GitHub Actions** (testes no back, build no front, badges) | Pre-requisito de qualquer arquitetura; pendencia antiga. | ~1h |
| 4 | **Disciplinas ja vigentes (manter, custo zero)** | Slices sem import cruzado; estado externalizado no Redis; stateless/JWT; SSE via Redis pub/sub (ADR 005); `room_players` como fonte de verdade. | 0 |

## O que explicitamente NAO fazer agora

Gateway, tokens internos entre servicos, um banco por servico, Kafka, service mesh — nada disso antes de um gatilho disparar. O Redis pub/sub do ADR 005 cobre o realtime na escala atual.

## Consequencias

- O desenvolvimento continua com um unico processo, um deploy e uma suite de testes — velocidade maxima para construir o dominio do jogo.
- A decisao de migrar deixa de ser ansiedade arquitetural e vira leitura de metrica contra os gatilhos deste ADR.
- Se um gatilho disparar, a extracao segue o mapa de costuras acima, com o dominio ja conhecido — o mesmo caminho que o crono-hub percorreu.