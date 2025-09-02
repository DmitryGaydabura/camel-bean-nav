# Camel Bean Navigator
<p align="center">
  <img src="src/main/resources/META-INF/pluginIcon.svg" alt="Camel Bean Navigator" width="160">
</p>

<p align="center">
  <a href="https://plugins.jetbrains.com/plugin/28351-camel-bean-nav">
    <img src="https://img.shields.io/jetbrains/plugin/v/org.gaydabura.camel-bean-nav.svg" alt="Version">
  </a>
  <a href="https://plugins.jetbrains.com/plugin/28351-camel-bean-nav">
    <img src="https://img.shields.io/jetbrains/plugin/d/org.gaydabura.camel-bean-nav.svg" alt="Downloads">
  </a>
  <a href="https://github.com/gaydabura/camel-bean-nav/blob/main/LICENSE">
    <img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="License">
  </a>
  <a href="https://plugins.jetbrains.com/plugin/28351-camel-bean-nav/reviews">
    <img src="https://img.shields.io/badge/reviews-★★★★★-brightgreen.svg" alt="Reviews">
  </a>
  <img src="https://img.shields.io/badge/Java-17+-orange.svg" alt="Java 17+">
  <img src="https://img.shields.io/badge/IntelliJ-2024.2+-purple.svg" alt="IntelliJ 2024.2+">
</p>

Jump from Apache Camel `.bean("beanName", "methodName(...)")` calls directly to the corresponding Spring bean and its method.  
Speeds up navigation and debugging in integration routes.

---

## ✨ Features
- **Go to declaration** from `"beanName"` → `@Bean` method or `@Service/@Component` class  
- **Go to declaration** from `"methodName(...)"` → matching `public` method in the bean class  
- **Method name completion** in the second argument of `.bean(...)`  

<p align="center">
  <img src="src/main/resources/META-INF/demo.gif" alt="Camel Bean Navigator GIF" >
</p>

---

## 🛠 Compatibility
- IntelliJ IDEA **2024.2+**  
- Java DSL supported  
- Kotlin DSL support is planned  

---

## 📦 Installation
- From IDE:  
  `Settings → Plugins → Marketplace → Search for "Camel Bean Navigator"`  
- Or download the latest release from [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/28351-camel-bean-nav) and install via:  
  `Settings → Plugins → ⚙ → Install Plugin from Disk…`
