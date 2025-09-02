# Camel Bean Navigator
<p align="center">
  <img src="src/main/resources/META-INF/pluginIcon.svg" alt="Camel Bean Navigator" width="160">
</p>

[![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/v/org.gaydabura.camel-bean-nav.svg)](https://plugins.jetbrains.com/plugin/28351-camel-bean-nav)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/org.gaydabura.camel-bean-nav.svg)](https://plugins.jetbrains.com/plugin/28351-camel-bean-nav)

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


