# BridgeNet / Services / Language

Permissions - Внутриигровой сервис удаленного доступа, имеющий в себе модель
<br>и реализацию индивидуальных прав и групп прав относительно игроков.

---

## MODEL

Для использования сервиса необходимо использовать модельный
<br>интерфейс `me.moonways.bridgenet.model.language.LanguageServiceModel`:

```java
@Inject
private LanguageServiceModel serviceModel;
```

С помощью данного интерфейса можно управлять как индивидуальными
<br>типами мировых языков относительно пользователей, так и использовать
<br>уже зарегистрированные ранее в системе:

```java
Language language = LanguageTypes.ITALY;

Component message = serviceModel.message(language, MessageTypes.GREETING_ON_JOIN);
```

---

## ENDPOINT

Конфигурационные данные, на которых базируется запуск сервиса
<br>под имплементаций эндпоинта:

```xml
<service>
    <!-- RMI Protocol service bind port -->
    <bindPort>7012</bindPort>
    <!-- Service direction name -->
    <name>language</name>
    <!-- Target service class type -->
    <modelPath>me.moonways.bridgenet.model.language.LanguageServiceModel</modelPath>
</service>
```

- Реализация эндпоинта лежит в модуле `endpoints/language`;
- Имплементацией основного модельного интерфейса сервиса 
  <br>является `me.moonways.endpoint.language.LanguageServiceEndpoint`;

Реализация использует фреймворк базы данных от Bridgenet. 
<br>Наименование используемых таблиц и сущностей

<br>

Examples: `player_languages` (`me.moonways.endpoint.language.EntityLanguage`)

| player_id                            | lang_id         |
|--------------------------------------|-----------------|
| 756d7e30-d173-4c85-8827-67aad3c258aa | bridgenet.test  |
| 04d15ffd-066e-4332-96ad-64670b7c56dd | leveling.max.30 |