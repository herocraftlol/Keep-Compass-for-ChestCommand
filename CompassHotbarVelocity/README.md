# CompassHotbarVelocity

Petit plugin **Velocity** (proxy uniquement) qui alimente le menu
"Serveurs & Mini-jeux" du plugin backend **CompassHotbar** (Spigot/Paper).

## Ce qu'il fait

- Toutes les 5 secondes, ping chaque serveur déclaré dans le
  `velocity.toml` du proxy (`[servers]`).
- Pour chaque serveur : nombre de joueurs, joueurs max, et "ouvert"
  (répond au ping) ou "fermé" (timeout/erreur).
- Envoie ce résumé (texte simple, pas de dépendance JSON) sur le canal
  plugin `compasshotbar:sync` à chaque serveur backend actuellement en
  ligne.

## Ce qu'il ne fait PAS

- Il ne gère aucune commande, aucune configuration.
- Il n'est pas responsable du transfert des joueurs d'un serveur à
  l'autre : ça, c'est le canal legacy `bungeecord:main` ("Connect"),
  déjà supporté nativement par Velocity, utilisé directement par
  CompassHotbar.

## Installation

```bash
mvn clean package
```

Placez `target/CompassHotbarVelocity-1.0.0.jar` dans le dossier
`plugins/` de votre **proxy Velocity** (et uniquement là — ne le mettez
pas sur vos serveurs de jeu, c'est un plugin proxy). Redémarrez le proxy.

Vérifiez dans les logs du proxy une ligne du type :

```
CompassHotbarVelocity activé : ping toutes les 5s, canal compasshotbar:sync enregistré.
```

## Compatibilité

Écrit contre `velocity-api` 3.3.0-SNAPSHOT (Velocity 3.x). Si votre
proxy utilise une autre version majeure de l'API Velocity, adaptez la
version dans `pom.xml` en conséquence.

Pour un proxy **BungeeCord**, ce plugin ne fonctionnera pas tel quel
(API différente) : il faudrait réécrire l'équivalent avec l'API
BungeeCord (même principe — ping des serveurs, même format de message
sur `compasshotbar:sync`). Le transfert de joueurs, lui, fonctionne
nativement avec BungeeCord sans rien à installer.
