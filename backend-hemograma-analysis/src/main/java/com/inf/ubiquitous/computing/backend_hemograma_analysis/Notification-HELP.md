# 📋 API de Notificações - Documentação
🚀 Endpoints Disponíveis
1. 📥 Obter Casos Recentes
   Retorna as 10 notificações mais recentes e contagem de não lidas

🔗 Endpoint
http
GET /api/notificacao/casos-recentes
📤 Resposta de Sucesso
````
{
"notificacoes": [
{
"id": 1,
"tipo": "ALERTA",
"mensagem": "Texto da notificação",
"lida": false,
"dataCriacao": "2024-01-15T10:30:00",
"regiao": "Norte"
}
],
"total": 10,
"naoLidas": 5
}
```` 
🎯 Campos da Resposta
Campo	Tipo	Descrição
notificacoes	Array	Lista das 10 notificações mais recentes
notificacoes[].id	Number	ID único da notificação
notificacoes[].tipo	String	Tipo da notificação (ALERTA, INFORMACAO, etc)
notificacoes[].mensagem	String	Texto descritivo da notificação
notificacoes[].lida	Boolean	Status de leitura da notificação
notificacoes[].dataCriacao	String	Data e hora de criação (ISO 8601)
notificacoes[].regiao	String	Região associada à notificação
total	Number	Total de notificações retornadas
naoLidas	Number	Quantidade de notificações não lidas


# Documentação WebSocket - Sistema de Notificações em Tempo Real
## 📋 Visão Geral
Este sistema permite que frontends recebam notificações em tempo real sobre casos de HIV detectados através de WebSocket. As notificações são enviadas automaticamente quando novos casos são processados pelo sistema.

🔌 Configuração do WebSocket URL de Conexão

ws://localhost:8080/ws-notificacoes
Fallback para navegadores antigos:

javascript
http://localhost:8080/ws-notificacoes
### Tópicos Disponíveis

- /topic/notificacoes-tempo-real	Notificações em tempo real (principal)
- /topic/notificacoes	Todas as notificações
- /topic/notificacoes/hiv	Apenas notificações de HIV
- /topic/notificacoes/estatisticas	Estatísticas atualizadas
- /topic/ultima.notificacao	Última notificação produzida
- /topic/nova.notificacao	Novas notificações (webhook)
- 
🚀 Implementação no Frontend
1. Conexão Básica com WebSocket
   javascript
   // Configuração básica
   const socket = new SockJS('http://localhost:8080/ws-notificacoes');
   const stompClient = Stomp.over(socket);

// Conectar
stompClient.connect({}, function(frame) {
console.log('✅ Conectado ao WebSocket:', frame);

    // Inscrever nos tópicos
    stompClient.subscribe('/topic/notificacoes-tempo-real', function(message) {
        const notificacao = JSON.parse(message.body);
        console.log('📨 Nova notificação:', notificacao);
        exibirNotificacao(notificacao);
    });

}, function(error) {
console.error('❌ Erro de conexão:', error);
});

📨 Estrutura das Mensagens
Notificação em Tempo Real
````
{
"tipo": "NOTIFICACAO_TEMPO_REAL",
"acao": "NOVA_NOTIFICACAO",
"timestamp": "2024-01-01T10:00:00",
"mensagem": "Nova notificação recebida em tempo real",
"data": {
"id": 123,
"tipo": "HIV_DETECTADO",
"hemogramaId": "HEMO-123",
"pacienteId": "PAC-456",
"regiao": "Sudeste",
"estado": "SP",
"faixaEtaria": "30-39",
"sexo": "M",
"motivoRisco": "Padrão hemograma suspeito",
"lida": false,
"dataCriacao": "2024-01-01T10:00:00"
}
}
````
````
Estatísticas
json
{
"tipo": "ESTATISTICAS_TEMPO_REAL",
"timestamp": "2024-01-01T10:00:00",
"totalNotificacoes": 150,
"naoLidas": 25,
"lidas": 125
}
````
###  🎯 Endpoints do WebSocket
Enviar Comandos (Client → Server)
Comando	Descrição
- /app/estatisticas.atual	Solicita estatísticas atualizadas
- /app/ultima.notificacao	Solicita a última notificação
- /app/notificacao.marcar-lida	Marca notificação como lida

- Receber Mensagens (Server → Client)
Tópico	Descrição
- /topic/notificacoes-tempo-real	Notificações principais
- /topic/estatisticas-tempo-real	Estatísticas atualizadas
- /topic/ultima.notificacao	Resposta da última notificação

🔧 Dependências
CDN (Recomendado para testes)
html
<script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
NPM (Para projetos React/Vue/Angular)
bash
npm install sockjs-client stompjs

yarn add sockjs-client stompjs
🐛 Solução de Problemas
Erro de Conexão
javascript
// Verifique:
// 1. Servidor Spring está rodando na porta 8080
// 2. URL do WebSocket está correta
// 3. Não há bloqueio de CORS

// Para desenvolvimento, pode ser necessário:
const socket = new SockJS('http://localhost:8080/ws-notificacoes');
Não Recebendo Notificações
Verifique se está inscrito nos tópicos corretos

Confirme que o Kafka está produzindo mensagens

Verifique os logs do servidor Spring

Reconexão Automática
javascript
// Adicione este código para reconexão automática
setInterval(() => {
if (!stompClient || !stompClient.connected) {
console.log('🔄 Reconectando...');
conectar();
}
}, 5000);
📱 Exemplo para React
jsx
import React, { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const NotificacoesComponent = () => {
const [notificacoes, setNotificacoes] = useState([]);
const [estatisticas, setEstatisticas] = useState({});
const [conectado, setConectado] = useState(false);

    useEffect(() => {
        const socket = new SockJS('http://localhost:8080/ws-notificacoes');
        const stompClient = new Client({
            webSocketFactory: () => socket,
            onConnect: () => {
                setConectado(true);
                
                stompClient.subscribe('/topic/notificacoes-tempo-real', (message) => {
                    const novaNotificacao = JSON.parse(message.body);
                    setNotificacoes(prev => [novaNotificacao, ...prev]);
                });

                stompClient.subscribe('/topic/estatisticas-tempo-real', (message) => {
                    setEstatisticas(JSON.parse(message.body));
                });
            },
            onDisconnect: () => setConectado(false)
        });

        stompClient.activate();

        return () => {
            stompClient.deactivate();
        };
    }, []);

    return (
        <div>
            <div>Status: {conectado ? '✅ Conectado' : '🔴 Desconectado'}</div>
            <div>Total de Notificações: {estatisticas.totalNotificacoes || 0}</div>
            {/* Renderizar notificações */}
        </div>
    );
};

export default NotificacoesComponent;