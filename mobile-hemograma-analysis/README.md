
# Projeto de Análise de Hemogramas - Mobile

## Tecnologias Utilizadas

- **React Native:** Framework para desenvolvimento de aplicativos móveis nativos.
- **Expo:** Plataforma e conjunto de ferramentas para facilitar o desenvolvimento e build com React Native.
- **React Navigation:** Biblioteca para gerenciamento de navegação e rotas (Stack e Drawer Navigator).
- **Axios:** Cliente HTTP para fazer requisições à API do backend.
- **Expo Secure Store:** Para armazenamento seguro de dados sensíveis, como tokens de autenticação.

---

## Pré-requisitos

Antes de começar, garanta que você tenha o seguinte instalado e configurado em sua máquina:

1.  **Node.js (versão LTS recomendada):** [https://nodejs.org/](https://nodejs.org/)
2.  **NPM ou Yarn:** Gerenciador de pacotes (geralmente instalado com o Node.js).
3.  **Expo Go App:** O aplicativo cliente no seu celular para rodar o projeto em desenvolvimento.
    -   [Para Android (Google Play)](https://play.google.com/store/apps/details?id=host.exp.exponent)
    -   [Para iOS (App Store)](https://apps.apple.com/us/app/expo-go/id982107779)
4.  **Backend Rodando:** O [backend em Spring Boot](https://github.com/StephanyMil/hemograma-analysis/tree/main/backend-hemograma-analysis) **deve estar em execução** na sua máquina local, pois o aplicativo móvel depende dele para funcionar.

---

## Configuração do Projeto

Siga os passos abaixo para configurar o ambiente de desenvolvimento local.

### Passo 1: Clonar o Repositório

```bash
git clone https://github.com/StephanyMil/hemograma-analysis.git
cd mobile-hemograma-analysis
```

### Passo 2: Instalar as Dependências

Este comando irá baixar todas as bibliotecas e pacotes necessários para o projeto.

```bash
npm install
```

### Passo 3: Configurar o Endereço do Backend (Passo Crítico!)

O aplicativo móvel precisa saber o endereço IP do seu computador na rede local para se comunicar com o backend. **Ele não consegue acessar `localhost`**.

1.  **Abra o arquivo:** `src/api/apiService.js`

2.  **Encontre a linha:**
    ```javascript
    const BASE_URL = 'http://192.168.1.10:8080'; // <-- MUDE ESTE VALOR
    ```

3.  **Descubra o seu endereço IP local:**
    -   **No Windows:** Abra o terminal (CMD ou PowerShell) e digite `ipconfig`. Procure pelo "Endereço IPv4" na sua conexão de rede (Wi-Fi ou Ethernet).
    -   **No Mac/Linux:** Abra o terminal e digite `ifconfig` ou `ip a`. Procure pelo endereço "inet" na sua conexão de rede.

4.  **Altere o valor da constante `BASE_URL`** para o seu endereço IP, mantendo a porta `8080`. Por exemplo:
    ```javascript
    const BASE_URL = 'http://192.168.0.15:8080';
    ```

---

## Rodando o Projeto

Com tudo configurado, você pode iniciar o aplicativo.

### Passo 1: Iniciar o Servidor de Desenvolvimento

No terminal, dentro da pasta do projeto, execute o comando:

```bash
npx expo start
```

Isso iniciará o Metro Bundler e exibirá um **QR Code** no terminal.

### Passo 2: Abrir o Aplicativo no seu Celular

1.  **Garanta que seu celular e seu computador estejam conectados na mesma rede Wi-Fi.**
2.  **No Android:** Abra o aplicativo **Expo Go** e escaneie o QR Code exibido no terminal.
3.  **No iOS:** Abra o aplicativo de **Câmera** e aponte para o QR Code. Um banner aparecerá para abrir o projeto no Expo Go.

O aplicativo será compilado e carregado no seu celular. Qualquer alteração que você fizer no código será refletida em tempo real.

---

## Estrutura de Pastas

O projeto segue a seguinte estrutura de pastas:

```
.
├── api/          # Centraliza as chamadas para o backend (apiService.js)
├── components/   # Componentes reutilizáveis (cards, menus, etc.)
├── constants/    # Constantes, como a paleta de cores (Colors.js)
├── context/      # Gerenciamento de estado global (AuthContext.js)
├── navigation/   # Configuração da navegação do app (AppNavigator.js)
└── screens/      # As telas principais do aplicativo (Login, Home, etc.)
├── assets/         # Imagens, fontes e outros arquivos estáticos
└── App.js          # Ponto de entrada principal do aplicativo
```

## Solução de Problemas Comuns

-   **Erro de Rede ("Network Error"):**
    -   Verifique se o endereço IP em `src/api/apiService.js` está correto.
    -   Confirme que seu celular e computador estão na mesma rede Wi-Fi.
    -   Verifique se o backend Spring Boot está realmente rodando na porta `8080`.

-   **O aplicativo trava ao iniciar ou se comporta de forma estranha:**
    -   Pare o servidor (Ctrl+C) e reinicie-o limpando o cache:
      ```bash
      npx expo start -c
      ```
