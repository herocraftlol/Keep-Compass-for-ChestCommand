# Keep Compass for ChestCommand

Plugin Minecraft Spigot/Paper 1.21+ qui garde automatiquement des items fixes et cliquables
dans la hotbar : une boussole, un accès boutique, un lien vers votre site, un lien Discord,
et un accès au menu amis.

## 📋 Fonctionnalités

- **Items fixes automatiques** : chaque item configuré est placé dans son slot dédié
- **Anti-drop** : impossible de lâcher un item géré par le plugin
- **Anti-déplacement** : impossible de le déplacer/échanger dans l'inventaire
- **Persistance** : les items reviennent après mort, respawn, reconnexion, ouverture d'inventaire
- **Actions au clic droit** :
  - **URL** : envoie un message cliquable dans le chat qui ouvre le lien dans le
    navigateur du joueur (Minecraft ne permet pas d'ouvrir un navigateur sans
    action du joueur, cliquer sur le message est donc l'équivalent le plus proche
    d'un lien "direct")
  - **Commande** : exécute une commande en tant que joueur (ex: `/friend gui`)
- **100% configurable** : slots, matériaux, noms, lores, liens et commandes se
  règlent dans `config.yml`, sans recompiler le plugin
- **Zone de restriction (optionnelle)** : les items ne s'affichent que dans une zone
  définie (par exemple un lobby). Définissez la zone avec `/compasshotbar pos1` et
  `/compasshotbar pos2`, puis activez avec `/compasshotbar zone enable`
- **Mode global (prioritaire)** : permet de désactiver temporairement la restriction
  de zone et d'afficher la hotbar partout sur le serveur, sans modifier la configuration
  de la zone. Utilisez `/compasshotbar global on` pour activer et `/compasshotbar global off`
  pour revenir au comportement normal
- **Commandes admin** : activez/désactivez le plugin avec `/compasshotbar`

## 🎒 Items fournis par défaut

| Item | Slot | Matériau | Action au clic droit |
|------|------|----------|----------------------|
| Boussole | 8 | Compass | Aucune (comportement vanilla) |
| Boutique | 0 | Emerald | Ouvre le lien de la boutique |
| Site Web | 1 | Cobweb (toile d'araignée, forcément 🕸️) | Ouvre le lien du site |
| Discord | 2 | Heart of the Sea (orbe de la mer) | Ouvre le lien d'invitation Discord |
| Amis | 3 | Player Head | Exécute `/friend gui` |

Vous pouvez désactiver, déplacer ou ajouter autant d'items que vous voulez (dans la limite
des 9 slots de la hotbar) directement dans `config.yml`.

## 🎮 Commandes

| Commande | Description |
|----------|-------------|
| /compasshotbar | Active ou désactive le plugin |
| /compasshotbar reload | Recharge la configuration (y compris les items) |
| /compasshotbar give | Redonne tous les items à tous les joueurs |
| /compasshotbar toggle | Alias pour activer/désactiver |
| /compasshotbar status | Affiche le statut du plugin |
| /compasshotbar pos1 | Définit le premier coin de la zone (bloc visé) |
| /compasshotbar pos2 | Définit le second coin de la zone (bloc visé) |
| /compasshotbar zone enable | Active la restriction de zone |
| /compasshotbar zone disable | Désactive la restriction de zone |
| /compasshotbar zone info | Affiche les infos de la zone définie |
| /compasshotbar global on | Active le mode global (hotbar partout) |
| /compasshotbar global off | Désactive le mode global |
| /compasshotbar global status | Affiche l'état du mode global |

## 📦 Installation

1. Compilez le JAR avec `mvn clean package` (ou téléchargez-le depuis vos Releases)
2. Placez le JAR dans le dossier `plugins/` de votre serveur Spigot/Paper 1.21+
3. Démarrez le serveur une première fois pour générer `plugins/CompassHotbar/config.yml`
4. Éditez ce fichier pour renseigner vos vrais liens (boutique, site, Discord)
5. `/compasshotbar reload`

## 📋 Permissions

- `compasshotbar.use` : donne les items automatiquement (par défaut: true)
- `compasshotbar.command` : permet d'utiliser les commandes admin (par défaut: op)

## ⚙️ Configuration

Modifiez `plugins/CompassHotbar/config.yml` :

```yaml
enabled: true

hotbar-items:
  shop:
    enabled: true
    slot: 0
    material: EMERALD
    name: "&a&lBoutique"
    lore:
      - "&7Clique-droit pour accéder"
      - "&7à la boutique du serveur"
    url: "https://votre-boutique.tebex.io"

  friends:
    enabled: true
    slot: 3
    material: PLAYER_HEAD
    name: "&d&lAmis"
    command: "friend gui"
```

Chaque item accepte : `enabled`, `slot` (0-8), `material`, `name`, `lore`, et **soit**
`url` **soit** `command` (pas les deux) pour définir son action au clic droit.

## 🔧 Compilation

```bash
mvn clean package
```

Le JAR sera dans `target/CompassHotbar-1.3.0.jar`.
