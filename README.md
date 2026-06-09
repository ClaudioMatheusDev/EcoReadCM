<div align="center">

# 🌿 EcoReadCM

### Sistema de Gestão de Consumo Predial

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-07405E?style=for-the-badge&logo=sqlite&logoColor=white)
![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=for-the-badge&logo=material-design&logoColor=white)

> Aplicativo Android para centralizar o cadastro de proprietários, unidades e leituras mensais de energia elétrica e gás em condomínios.

</div>

---

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Funcionalidades](#funcionalidades)
- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Como Executar](#como-executar)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Modelo de Dados](#modelo-de-dados)
- [Telas](#telas)
- [Melhorias Futuras](#melhorias-futuras)
- [Autor](#autor)

---

## 📖 Sobre o Projeto

A gestão manual de leituras de utilidades (luz e gás) em condomínios é propensa a erros e dificulta a análise histórica de consumo. O **EcoReadCM** resolve isso centralizando:

- O cadastro de **proprietários** e seus respectivos **apartamentos**
- O registro mensal de **leituras de luz (kWh) e gás (m³)**
- A **consulta de médias** dos últimos 3 ou 6 meses
- O acompanhamento de **quais unidades já foram lidas** no mês

---

## ✅ Funcionalidades

| RF | Funcionalidade | Status |
|----|----------------|--------|
| RF01 | Cadastro de proprietários (nome, CPF, contato) | ✅ |
| RF02 | Vínculo de múltiplos apartamentos a um proprietário | ✅ |
| RF03 | Registro mensal de leitura de luz (kWh) e gás (m³) | ✅ |
| RF04 | Consulta da última leitura e média (últimos 3 ou 6 meses) | ✅ |
| RF05 | Relatório de apartamentos com e sem leitura no mês | ✅ |

---

## 🏗️ Arquitetura

O projeto segue o padrão **DAO (Data Access Object)**, garantindo separação clara entre a camada de interface, a lógica de negócio e o acesso ao banco de dados.

```
┌─────────────────────────────────────────────────────┐
│                   UI (Activities)                    │
│  MainActivity · ProprietariosActivity · ...          │
└───────────────────────┬─────────────────────────────┘
                        │ usa interfaces
┌───────────────────────▼─────────────────────────────┐
│               Interfaces DAO                         │
│  IProprietarioDAO · IApartamentoDAO · ILeituraDAO    │
└───────────────────────┬─────────────────────────────┘
                        │ implementa
┌───────────────────────▼─────────────────────────────┐
│              Implementações DAO                      │
│  ProprietarioDAOImpl · ApartamentoDAOImpl · ...      │
└───────────────────────┬─────────────────────────────┘
                        │ acessa
┌───────────────────────▼─────────────────────────────┐
│         DatabaseHelper (SQLite)                      │
│         ecoread.db                                   │
└─────────────────────────────────────────────────────┘
```

> **Regra**: A camada de UI nunca acessa o banco de dados diretamente. Todo acesso passa obrigatoriamente pelas interfaces DAO.

---

## 🛠️ Tecnologias

- **Java** — linguagem principal
- **Android SDK** — minSdk 24 / targetSdk 34
- **SQLite** — banco de dados local via `SQLiteOpenHelper`
- **Material Design 3** — componentes de UI (`material:1.11.0`)
- **RecyclerView** — listas de proprietários, apartamentos e leituras
- **CardView** — cards de informação nas telas
- **Gradle KTS** — sistema de build

---

## 📋 Pré-requisitos

- Android Studio **Hedgehog** ou superior
- JDK **8** ou superior
- Android SDK com API 24+
- Dispositivo ou emulador com Android 7.0+

---

## 🚀 Como Executar

```bash
# 1. Clone o repositório
git clone https://github.com/ClaudioMatheusDev/EcoReadCM.git

# 2. Abra no Android Studio
# File → Open → selecione a pasta EcoReadCM

# 3. Aguarde o Gradle sincronizar as dependências

# 4. Execute no emulador ou dispositivo
# Run → Run 'app'  (Shift + F10)
```

---

## 📁 Estrutura do Projeto

```
app/src/main/
├── java/com/example/ecoreadcm/
│   ├── model/
│   │   ├── Proprietario.java       # Entidade proprietário
│   │   ├── Apartamento.java        # Entidade apartamento
│   │   └── Leitura.java            # Entidade leitura mensal
│   │
│   ├── database/
│   │   └── DatabaseHelper.java     # Criação e migração do SQLite
│   │
│   ├── dao/
│   │   ├── IProprietarioDAO.java   # Interface DAO proprietário
│   │   ├── IApartamentoDAO.java    # Interface DAO apartamento
│   │   ├── ILeituraDAO.java        # Interface DAO leitura
│   │   └── impl/
│   │       ├── ProprietarioDAOImpl.java
│   │       ├── ApartamentoDAOImpl.java
│   │       └── LeituraDAOImpl.java
│   │
│   └── ui/
│       ├── MainActivity.java               # Dashboard
│       ├── ProprietariosActivity.java      # Lista e cadastro (RF01)
│       ├── ProprietarioDetalheActivity.java # Vínculo de unidades (RF02)
│       ├── ApartamentosActivity.java        # Lista de unidades
│       ├── ApartamentoDetalheActivity.java  # Leituras e médias (RF03/RF04)
│       ├── NovaLeituraActivity.java         # Registro de leitura (RF03)
│       ├── LeiturasDoMesActivity.java       # Relatório mensal (RF05)
│       └── adapters/
│           ├── ProprietarioAdapter.java
│           ├── ApartamentoAdapter.java
│           ├── LeituraAdapter.java
│           └── LeituraDoMesAdapter.java
│
└── res/
    ├── layout/         # XMLs de telas e itens de lista
    ├── values/         # cores, strings, temas
    └── drawable/       # shapes e backgrounds
```

---

## 🗄️ Modelo de Dados

```
┌─────────────────┐       ┌──────────────────┐       ┌─────────────────────┐
│  Proprietario   │       │   Apartamento    │       │       Leitura        │
├─────────────────┤       ├──────────────────┤       ├─────────────────────┤
│ id (PK)         │ 1   N │ id (PK)          │ 1   N │ id (PK)             │
│ nome            ├──────►│ numero           ├──────►│ apartamento_id (FK) │
│ cpf (UNIQUE)    │       │ bloco            │       │ mes                 │
│ contato         │       │ proprietario_id  │       │ ano                 │
└─────────────────┘       │   (FK)           │       │ valor_luz (kWh)     │
                          └──────────────────┘       │ valor_gas (m³)      │
                                                     │ UNIQUE(apt,mes,ano) │
                                                     └─────────────────────┘
```

**Constraints implementadas:**
- CPF único por proprietário
- Uma leitura por apartamento por mês/ano
- Exclusão em cascata: deletar proprietário remove seus apartamentos e leituras

---

## 📱 Telas

| Tela | Descrição |
|------|-----------|
| **Dashboard** | Visão geral com médias de consumo e lista de proprietários |
| **Proprietários** | Lista com busca, cadastro e edição (RF01) |
| **Detalhe do Proprietário** | Apartamentos vinculados ao proprietário (RF02) |
| **Detalhe do Apartamento** | Histórico de leituras e médias (3 ou 6 meses) com alerta de dados insuficientes (RF03/RF04) |
| **Nova Leitura** | Formulário com validação de duplicidade (RF03) |
| **Leituras do Mês** | Relatório com status OK/Pendente e navegação por mês (RF05) |

---

## 🔮 Melhorias Futuras

- [ ] Exportar relatório mensal em PDF
- [ ] Gráfico de evolução de consumo por apartamento
- [ ] Notificação de lembrete para coleta de leituras
- [ ] Backup automático do banco de dados
- [ ] Filtro de leituras por período customizado
- [ ] Suporte a múltiplos condomínios
- [ ] Autenticação por usuário

---

## 👨‍💻 Autor

**Claudio Matheus**

[![GitHub](https://img.shields.io/badge/GitHub-ClaudioMatheusDev-181717?style=flat&logo=github)](https://github.com/ClaudioMatheusDev)

---

<div align="center">
  Feito com ☕ Java e Android Studio
</div>
