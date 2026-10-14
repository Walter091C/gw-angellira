# gw-angellira

Biblioteca Java para o Webservice SOAP AngelLira. Beans e stub são gerados do
WSDL versionado em `src/main/resources/wsdl/WSImport.wsdl`; não edite os fontes
gerados em `build/generated/`.

## Build e consumo

Use JDK 17 ou superior compatível com Gradle 8.13 para executar o build.
Os fontes são compilados com source/target Java 8 e a stack SOAP usa `javax.*`
(CXF 3.5.11), mantendo o contrato original do projeto. Os testes foram executados
com JDK 17; o runtime do Webtrans ainda precisa ser validado na integração.

```sh
bash gradlew clean build
# Opcional: instalar no Maven local para consumir em outro projeto da máquina.
bash gradlew publishToMavenLocal
```

O build gera `build/libs/gw-angellira-1.0.0.jar` e o JAR de fontes. Coordenadas:
`br.com.gwsistemas:gw-angellira:1.0.0`. Consuma por Maven/Gradle para receber
as dependências transitivas; o JAR isolado não inclui CXF, JAXB e suas APIs.
`maven-publish` gera POM e metadados; nenhum repositório remoto de publicação
foi configurado. O WSDL é empacotado no JAR e a inicialização não o baixa da API.

```groovy
repositories { mavenLocal(); mavenCentral() }
dependencies { implementation 'br.com.gwsistemas:gw-angellira:1.0.0' }
```

## Uso

```java
AngelLiraConfig config = new AngelLiraConfig(
    AngelLiraConfig.DEFAULT_ENDPOINT,
    loginDaFilial, senhaDaFilial,
    true,  // homologacao: escolha conforme o ambiente/contrato
    false, // api_interna: escolha conforme o ambiente/contrato
    10000, 60000 // conexão e resposta, em milissegundos
);

try (AngelLiraClient client = new AngelLiraClient(config)) {
    Cliente cliente = new Cliente();
    cliente.setId(cnpjOuCpf); // identificador de correlação enviado pelo Webtrans
    cliente.setNome(nomeOuRazaoSocial);
    cliente.setFantasia(nomeFantasia);
    // Preencha os demais campos e envie só CNPJ (J) ou CPF (F).
    cliente.setCnpj(cnpj);
    ArrayOfRetornoCliente retorno = client.cadastrarClientes(
        java.util.Collections.singletonList(cliente));
    // Inspecione status/motivo de CADA retorno e correlacione pelo id.
    // Não persista sucesso somente porque a chamada HTTP terminou.
}
```

Imports públicos: `br.com.gwsistemas.angellira.config.AngelLiraConfig`,
`br.com.gwsistemas.angellira.client.AngelLiraClient` e os beans de
`br.com.gwsistemas.angellira.generated`.

| Método da lib | Operação SOAP | Entrada |
| --- | --- | --- |
| `cadastrarClientes` | `CadastrarCliente` | `List<Cliente>` em uma requisição |
| `consultarRotas` | `GetRotas` | `PesquisaRota`, com origem/destino e entregas opcionais |
| `consultarProcedimentosEmbarque` | `GetProcedimentoEmbarque` | Sem parâmetros |
| `validarViagem` | `ValidarViagem` | `Agendamento`, `Element` ou XML interno de `_Viagens` |
| `agendarViagem` | `AgendarViagem` | `Agendamento`, `Element` ou XML interno de `_Viagens` |
| `cancelarViagens` | `CancelarViagem` | `List<Cancelamento>` com carga e procedimento |
| `finalizarViagem` | `FinalizarViagem` | `IdentificadorViagem`, `XMLGregorianCalendar`, observação |

Todos usam `ValidationSoapHeader` com `userCod`, `userPwd`, `homologacao`
e `api_interna`. O transporte tem timeouts, não segue redirecionamentos e
não faz retransmissões automáticas de operações. Use HTTPS para a API real.
A instância não deve ser compartilhada entre threads. `getPort()` mantém
acesso às demais operações e aos parâmetros completos de `GetRotas`.

Os retornos gerados são preservados sem interpretar códigos de status ainda
não documentados. Falhas SOAP/transporte continuam sendo exceções JAX-WS,
inclusive `SOAPFaultException`; não são convertidas em sucesso ou lista vazia.
Evite registrar envelopes, credenciais e dados pessoais nos logs do consumidor.
Uma falha de transporte pode ocorrer depois de a API processar uma operação:
reconcilie o resultado antes de decidir pelo reenvio.

## Viagens tipadas e validadas

Os beans `Agendamento`, `Viagem`, `Entrega`, `Docto` e `TipoViagem` ficam em
`br.com.gwsistemas.angellira.model`. Eles cobrem os campos da demanda 7048 e
permitem reutilizar a lib tanto no manifesto quanto no romaneio. Listas de
viagens, entregas e documentos preservam a ordem de inserção.

```java
Agendamento agendamento = new Agendamento();
Viagem viagem = new Viagem();
viagem.setTipo(TipoViagem.TRANSFERENCIA);
viagem.setCarga("000006-002-12345");
viagem.setOrigem(codigoExternoUnidadeOrigem);
viagem.setDestino(codigoExternoUnidadeDestino);
viagem.setRota(codigoRotaAngelLira); // opcional, conforme procedimento
viagem.setPrevInicio(dataInicioSm); // XMLGregorianCalendar com data/hora
viagem.setDataFat(dataEmissao);
viagem.setVeiculo(placaCavalo);
viagem.setMotorista(cpfMotorista);
viagem.setValor(valorCarga); // BigDecimal; não passar valor formatado com vírgula
viagem.setFrete(valorFrete);
viagem.setVolume(volumeCarga);

Entrega entrega = new Entrega();
entrega.setCliente(cnpjOuCpfDestinatarioCadastrado);
entrega.setSeqEntrega(1);
entrega.setPrevInicio(dataInicioSm);
Docto documento = new Docto();
documento.setNumero(numeroCte); // Integer positivo; D_Numero é inteiro no XSD
// A série é texto e preserva zeros à esquerda.
documento.setSerie(serieCte);
entrega.getDocumentos().add(documento);
viagem.getEntregas().add(entrega);
agendamento.getViagens().add(viagem);

try (AngelLiraClient client = new AngelLiraClient(config)) {
    ArrayOfRetornoViagem retorno = client.validarViagem(agendamento);
    // Conferir status/motivo e correlacionar carga/código antes de persistir.
}
```

`AgendamentoXml.toElement` produz `<Agendamento><Versao>1.2</Versao>...`,
sem namespace, com decimais em ponto e datas `xs:dateTime`. `V_EnviarEmail`
e `V_EnviarSMS` têm padrão `N`. O documento completo é validado contra
`src/main/resources/xsd/Agendamento.xsd` antes do envio tipado. O esquema foi
fornecido pelo usuário, com a formatação normalizada e as regras preservadas.
Embora declare `tVersao` com padrão 1.0, o elemento `Versao` usa `xs:string`,
portanto 1.2 (exigido pela documentação da demanda) é aceito pelo XSD fornecido.

A validação exige viagens, carga/origem/destino, data de início e um identificador
de veículo; rejeita rota junto de polilinha, múltiplos identificadores de veículo,
motorista junto de senha, valores negativos e números de documento inválidos.
Exige entrega se origem e destino forem iguais e limita observações a 140
caracteres conforme o manual. O XSD tipa as placas como `xs:string` nos elementos
utilizados: a lib não impõe o regex antigo da demanda, que excluiria placas
Mercosul e identificadores FOX.

Para outros campos opcionais do manual, as sobrecargas XML continuam disponíveis.
O WSDL declara `_Viagens` com `xs:any`; passe a raiz **interna** de `_Viagens`,
sem Envelope SOAP ou wrapper da operação. A sobrecarga String rejeita DTDs e
entidades externas; a sobrecarga Element é para DOM confiável. Essas sobrecargas
não fazem validação XSD automática, diferentemente da entrada `Agendamento`.
Não houve chamada à API real nem homologação com credenciais.

## Responsabilidades do Webtrans e divergências da demanda 7048

- Ler e validar a configuração da filial; consultar dados de manifesto/romaneio,
  mapear os beans, agregar valores/volumes/frete e ordenar entregas.
- Enviar clientes ainda não cadastrados em lote, tratar falhas parciais e
  persistir as referências retornadas nos clientes corretos.
- `GetRotas` consulta uma pesquisa por chamada. O WSDL não possui operação de
  cadastro de rotas em lote. Não criar um lote inexistente para cumprir o trecho
  divergente da demanda; consultar/persistir códigos conforme o contrato.
- O WSDL `Cliente` possui `nome`, mas não `razaoSocial`: usar `nome` para o nome
  ou razão social conforme o tipo do cliente. CNPJ/CPF são campos separados.
- Circuito é 3 segundo o manual e o XSD; o valor 9 citado na demanda pertence
  ao tipo de transporte do Webtrans. A demanda também cita `V_Carga` com e sem hífens. Usar exatamente o mesmo identificador
  no envio, cancelamento e finalização após definir o formato com a integradora.
- Consultar procedimentos para a escolha do usuário. Na finalização, enviar a
  data atual com fuso definido pelo Webtrans, sem presumir o fuso da máquina.
- Confirmar o significado dos status e se `ValidarViagem` já cria a SM ou apenas
  valida antes de `AgendarViagem`; a biblioteca disponibiliza ambas separadamente.

## Testes

`bash gradlew test` executa testes com um servidor SOAP local: cabeçalhos e
SOAPAction, clientes em lote e retornos parciais, rotas, procedimentos, XML livre,
cancelamento, finalização com fuso e SOAP Fault. Também verifica os agendamentos tipados contra o XSD, namespace do payload,
exclusões entre campos, valores inválidos, timeout efetivo e configuração,
localização do WSDL no classpath e rejeição de XML inválido/XXE. Não requer
credenciais ou acesso à AngelLira; requer downloads de dependências no primeiro
build. A homologação real e a integração com Webtrans continuam separadas.
