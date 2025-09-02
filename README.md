# Camel Bean Navigator

[![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/v/org.gaydabura.camel-bean-nav.svg)](https://plugins.jetbrains.com/plugin/28351-camel-bean-nav)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/org.gaydabura.camel-bean-nav.svg)](https://plugins.jetbrains.com/plugin/28351-camel-bean-nav)

Jump from Apache Camel `.bean("beanName", "methodName(...)")` calls directly to the corresponding Spring bean and its method.  
Speeds up navigation and debugging in integration routes.

---

## ✨ Features
- **Go to declaration** from `"beanName"` → `@Bean` method or `@Service/@Component` class  
- **Go to declaration** from `"methodName(...)"` → matching `public` method in the bean class  
- **Method name completion** in the second argument of `.bean(...)`  

---

## 🖼 Screenshots
_Add screenshots or a GIF once the plugin is published._  

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

---

## 📊 Marketplace Widget

Use these official JetBrains Marketplace widgets on your website:

```html
<script src="https://plugins.jetbrains.com/assets/scripts/mp-widget.js"></script>
<script>
  // Replace #yourelement with a real element id on your webpage
  MarketplaceWidget.setupMarketplaceWidget('card', 28351, "#yourelement");
</script>

<script src="https://plugins.jetbrains.com/assets/scripts/mp-widget.js"></script>
<script>
  // Replace #yourelement with a real element id on your webpage
  MarketplaceWidget.setupMarketplaceWidget('install', 28351, "#yourelement");
</script>
