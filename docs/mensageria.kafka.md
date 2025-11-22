[#XXXXX] (MELHORIA) Implementa notificação assíncrona via Kafka no backend

## Objetivo

Implementar uma notificação assíncrona usando **Kafka** para registrar/propagar o evento de `<descrever evento, ex: criação/atualização de X>` no backend, evitando acoplamento direto entre os módulos e permitindo tratamento posterior do evento por outros serviços.

## O que será feito

- Configurar o **producer** Kafka no backend para publicar mensagens no tópico `<NOME_DO_TOPICO>`.
- Definir o **payload da mensagem** (DTO) com as informações necessárias para a notificação:
  - `<campo_1>` (ex: identificador do recurso)
  - `<campo_2>` (ex: tipo de operação: CREATE/UPDATE/DELETE)
  - `<campo_3>` (ex: data/hora do evento)
  - Outros campos relevantes para o consumo da notificação.
- Criar um **serviço de publicação** no backend responsável por:
  - Montar o DTO da mensagem.
  - Serializar o conteúdo (ex: JSON).
  - Enviar a mensagem para o tópico Kafka configurado.
- Integrar a publicação da mensagem nos pontos de negócio necessários:
  - Após `<ação principal>` (ex: salvar novo registro / atualizar status / realizar operação de negócio).
  - Garantir que o envio para o Kafka não quebre o fluxo principal em caso de erro (logar erro, tratar fallback se necessário).
- (Opcional, se fizer parte da tarefa) Configurar um **consumer** Kafka para:
  - Ler mensagens do tópico `<NOME_DO_TOPICO>`.
  - Processar a notificação (ex: gravação em log, envio de e-mail, atualização de outro módulo, etc.).
- Adicionar **logs** para facilitar rastreio das mensagens publicadas e consumidas.
- Criar/ajustar **testes**:
  - Testes de unidade para o serviço de publicação.
  - Testes de integração (se possível) para validar envio/consumo da mensagem.

## Detalhes técnicos

- Adicionar/validar dependências Kafka no projeto backend (ex: Spring Kafka).
- Configurar propriedades de conexão com o broker Kafka:
  - `bootstrap.servers`
  - configuração de serializers/deserializers
  - nome do tópico padrão (se aplicável).
- Definir estratégia de tratamento de erro:
  - Logar falhas de publicação.
  - Avaliar se será necessário retry, dead-letter ou apenas log.
- Garantir que a estrutura do DTO está compatível com o que outros serviços/consumidores esperam (se já existir contrato).

## Como testar

1. Subir o ambiente com **Kafka** ativo.
2. Executar a ação no sistema que dispara a notificação:
   - Ex: criar/atualizar/excluir `<entidade>` na tela `<CÓDIGO/NOME_DA_TELA>`.
3. Validar que:
   - A requisição principal conclui com sucesso (sem erro no fluxo do usuário).
   - A mensagem é enviada para o tópico `<NOME_DO_TOPICO>`:
     - Verificando via ferramenta de consumo Kafka (console / UI).
4. (Se houver consumer implementado) Confirmar que:
   - A mensagem foi consumida.
   - A lógica de processamento foi executada corretamente (ex: log, gravação, outra ação de negócio).

## Possíveis impactos

- Impacto em performance limitado ao momento de publicação da mensagem no Kafka após `<ação principal>`.
- Dependência do serviço Kafka para o envio da notificação:
  - Em caso de indisponibilidade do Kafka, o fluxo principal **não deve** quebrar (apenas logar a falha).
- Novos pontos de integração com outros serviços que venham a consumir o tópico `<NOME_DO_TOPICO>`.
