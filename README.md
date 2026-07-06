# Keep Compass for ChestCommand

Plugin Minecraft Spigot/Paper 1.21+ qui garde automatiquement une boussole dans le dernier slot de la hotbar (slot 9).

## 📋 Fonctionnalités

- **Boussole automatique** : Une boussole est automatiquement placée dans le slot 8 (dernier de la hotbar)
- **Anti-drop** : Impossible de lâcher la boussole
- **Anti-déplacement** : Empêche de déplacer ou échanger la boussole avec d'autres items
- **Persistance après mort** : La boussole reste dans l'inventaire après la mort du joueur
- **Persistance** : La boussole revient après mort, respawn, reconnexion
- **Commandes complètes** : Activez/désactivez le plugin avec /compasshotbar

## 🎮 Commandes

| Commande | Description |
|----------|-------------|
| /compasshotbar | Active ou désactive le plugin |
| /compasshotbar reload | Recharge la configuration |
| /compasshotbar give | Redonne une boussole à tous les joueurs |
| /compasshotbar toggle | Alias pour activer/désactiver |
| /compasshotbar status | Affiche le statut du plugin |

## 📦 Installation

1. Téléchargez le JAR depuis les [Releases](https://github.com/herocraftlol/Keep-Compass-for-ChestCommand/releases)
2. Placez le JAR dans le dossier `plugins/` de votre serveur Spigot/Paper 1.21+
3. Redémarrez le serveur

## 📋 Permissions

- `compasshotbar.use` : Donne la boussole automatiquement (par défaut: true)
- `compasshotbar.command` : Permet d'utiliser les commandes admin (par défaut: op)

## ⚙️ Configuration

Modifiez `plugins/CompassHotbar/config.yml` :

```yaml
enabled: true
```

## 🔧 Compilation

```bash
mvn clean package
```

Le JAR sera dans `target/CompassHotbar-1.0.0.jar`.
