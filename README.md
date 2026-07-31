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
- **Mode "Kill Outside"** : si activé, tout joueur qui sort de la zone définie est
  immédiatement éliminé. Idéal pour les modes arène ou guerre où quitter la zone est fatal.
  Les joueurs avec la permission `compasshotbar.zone.bypass` sont免疫. Commandes :
  `/compasshotbar zone killoutside enable` et `/compasshotbar zone killoutside disable`
- **Items avec paramètres de zone individuels** : chaque item peut être configuré pour
  s'afficher ou non en dehors de la zone définie via le paramètre `showOutsideZone`
- **Commandes admin** : activez/désactivez le plugin avec `/compasshotbar`
- **Menu "Serveurs & Mini-jeux" sur la boussole** : menu personnalisable
  (icônes, noms, lores) listant les serveurs du réseau avec leur statut
  ouvert/fermé et leur nombre de joueurs en direct, compatible Velocity
  (voir section dédiée plus bas)

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
| /compasshotbar zone killoutside enable | Active le mode kill-outside (tue les joueurs qui sortent) |
| /compasshotbar zone killoutside disable | Désactive le mode kill-outside |
| /compasshotbar global on | Active le mode global (hotbar partout) |
| /compasshotbar global off | Désactive le mode global |
| /compasshotbar global status | Affiche l'état du mode global |

## 📦 Installation

1. Compilez le JAR avec `mvn clean package` (ou téléchargez-le depuis vos Releases)
2. Placez le JAR dans le dossier `plugins/` de votre serveur Spigot/Paper 1.21+
3. Démarrez le serveur une première fois pour générer `plugins/CompassHotbar/config.yml`
4. Éditez ce fichier pour renseigner vos vrais liens (boutique, site, Discord)
5. `/compasshotbar reload`
6. *(Optionnel mais recommandé)* Compilez et installez aussi
   `CompassHotbarVelocity` sur votre **proxy** pour que le menu affiche les
   vrais statuts/joueurs (voir section "Menu Serveurs & Mini-jeux" plus bas)

## 📋 Permissions

- `compasshotbar.use` : donne les items automatiquement (par défaut: true)
- `compasshotbar.command` : permet d'utiliser les commandes admin (par défaut: op)
- `compasshotbar.zone.bypass` : exempté de la fonctionnalité kill-outside (par défaut: op)

## ⚙️ Configuration

Modifiez `plugins/CompassHotbar/config.yml` :

```yaml
enabled: true

zone:
  enabled: false
  kill-outside: false          # Si true, tue le joueur qui sort de la zone
  kill-outside-message: "&6[CompassHotbar] &cVous êtes sorti de la zone, vous avez été éliminé !"

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

## 🧭 Menu "Serveurs & Mini-jeux" (clic sur la boussole)

Depuis la 1.5.0, la boussole peut ouvrir un menu (inventaire) entièrement
personnalisable listant les serveurs/mini-jeux de votre réseau, avec pour
chacun :

- une icône (matériau) et un nom personnalisables,
- son statut en direct : **ouvert** (répond au ping) ou **fermé**,
- son nombre de joueurs en direct (`{online}/{max}`),
- un effet "brillant" optionnel quand il est ouvert,
- un clic dessus transfère le joueur vers ce serveur via le proxy.

Tout se règle dans la section `servers-gui` de `config.yml` : titre, nombre
de lignes, matériau de remplissage, textes (avec les placeholders `{online}`
et `{max}`), et la liste des icônes (`items`), chacune avec son `slot`, son
`material`, son `name`, son `extra-lore`, et surtout son champ **`server`**
qui doit correspondre **exactement** au nom du serveur déclaré dans le
`velocity.toml` de votre proxy (section `[servers]`).

Pour que la boussole ouvre ce menu au lieu de ne rien faire, l'item
`compass` a maintenant un champ `opens-gui: true` dans `config.yml`
(passez-le à `false` pour revenir au comportement vanilla).

### D'où viennent les données de joueurs/statut ?

Un serveur Spigot/Paper seul ne peut pas savoir combien de joueurs sont
connectés sur *un autre* serveur du réseau : cette info doit venir du
proxy. C'est le rôle du petit plugin **CompassHotbarVelocity**, fourni à
côté de celui-ci (dossier `CompassHotbarVelocity/`) :

1. Compilez-le (`cd CompassHotbarVelocity && mvn clean package`) et placez
   le JAR obtenu dans `plugins/` de votre **proxy Velocity** (pas sur les
   serveurs de jeu).
2. Il ping automatiquement, toutes les 5 secondes, tous les serveurs
   déclarés dans `velocity.toml`, et pousse le résultat (nom, joueurs,
   joueurs max, ouvert/fermé) à CompassHotbar sur chaque serveur backend
   via le canal `compasshotbar:sync`.
3. Rien à configurer côté Velocity : aucune commande, aucun fichier de
   config. Un simple log au démarrage confirme qu'il tourne.

**Sans ce plugin sur le proxy**, le menu reste utilisable (les clics
transfèrent bien les joueurs), mais chaque icône affiche `Chargement...`
au lieu des vrais chiffres (voir `no-data-lore` dans la config).

Le transfert d'un joueur vers un autre serveur (clic dans le menu), lui,
**ne nécessite pas** CompassHotbarVelocity : il utilise le canal legacy
`bungeecord:main` ("Connect"), supporté nativement par Velocity (et
BungeeCord) sans aucun plugin.

### Compatibilité proxy

Ce système fonctionne aussi bien avec **Velocity** qu'avec **BungeeCord**
pour la partie transfert de joueurs (canal `bungeecord:main` standard).
Le ping en temps réel (ouvert/fermé + joueurs) nécessite en revanche le
plugin `CompassHotbarVelocity`, écrit spécifiquement pour l'API Velocity ;
sur BungeeCord il faudrait un équivalent utilisant l'API BungeeCord (même
principe : ping des serveurs + envoi du même format texte sur
`compasshotbar:sync`).

## 🔧 Compilation

```bash
mvn clean package
```

Le JAR sera dans `target/CompassHotbar-1.5.1.jar`.

Pour le plugin proxy (facultatif mais recommandé pour les vrais chiffres) :

```bash
cd CompassHotbarVelocity
mvn clean package
```

Le JAR sera dans `CompassHotbarVelocity/target/CompassHotbarVelocity-1.0.0.jar`.
