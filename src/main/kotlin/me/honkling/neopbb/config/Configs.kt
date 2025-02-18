package me.honkling.neopbb.config

enum class Configs(val fileName: String, val reload: () -> Unit) {
    Config("config.toml", ::loadConfigToml),
    Items("items.toml", ::loadItemsToml),
    Prisons("prisons.toml", ::loadPrisonsToml)
}