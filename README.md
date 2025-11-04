# Car Rent

![Spring Boot](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white)

## 📖 Sobre

Uma breve descrição do que esta API faz, qual problema ela resolve e seu principal objetivo. Este é o back-end do projeto Car Rent, que consome esta API através de uma aplicação front-end em Angular.

---

## 🛠️ Tecnologias Utilizadas

Liste as principais tecnologias, frameworks e bancos de dados que você usou no **back-end**:

* **Java 21**
* **Spring Boot 3.4.3**
* **Maven** 
* **SQLite**
* **Spring Security / JWT**
* **JPA / Hibernate**

---

## 🏁 Pré-requisitos

Antes de começar, você precisará ter as seguintes ferramentas instaladas em sua máquina:

* [Java JDK](https://www.oracle.com/java/technologies/downloads/) `[versão 21]`.
* [Maven](https://maven.apache.org/download.cgi).
* [Git](https://git-scm.com/downloads).
* (Para o front-end) [Node.js](https://nodejs.org/en/) e [Angular CLI](https://angular.io/cli).

---

## 🚀 Rodando a API (Back-end)

Siga os passos abaixo para executar o back-end localmente:

1. **Clone este repositório:**
    ```bash
    git clone https://github.com/Rennanml/car-rent.git
    cd car-rent
    ```
    
2. **Instale as dependências (se usar Maven):**
    ```bash
    mvn clean install
    ```

3. **Execute a aplicação (se usar Maven):**
    ```bash
    mvn spring-boot:run
    ```

> A API estará disponível em `http://localhost:8080`

---

## 🖥️ Rodando a Aplicação (Front-end)

O front-end desta aplicação foi desenvolvido em Angular e está em um **repositório separado**. Siga os passos abaixo para executá-lo:

1.  **Clone o repositório do front-end:**
    ```bash
    git clone https://github.com/Rennanml/car-rent-ui.git
    ```

2.  **Acesse a pasta do projeto:**
    ```bash
    cd car-rent-ui
    ```

3.  **Instale as dependências do Node.js:**
    ```bash
    npm install
    ```

4.  **Execute a aplicação Angular:**
    ```bash
    ng serve
    ```
    ou
    ```bash
    npm run start
    ```    


> A aplicação front-end estará disponível em `http://localhost:4200/` e fará as requisições para a API back-end (que deve estar rodando em `http://localhost:8080`).


## 🧑‍💻 Autores

**Abel Baes Correia**

* [GitHub](https://github.com/abelBaes)

**Felipe Marins Vilela**

* [GitHub](https://github.com/Felipe-Vilela)

**Rennan Marcile Lazarini**

* [GitHub](https://github.com/Rennanml)
