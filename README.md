# Application Algorithmes de Graphes
**AUTEURS : Hugo VARAO GOMES  DA SILVA / Valentin LEMAITRE**

Projet réalisé en BUT Informatique 2ème année.
Application Java Swing qui permet de saisir un graphe, choisir un algorithme
de plus court chemin, visualiser le résultat et voir le déroulé pas à pas.

---

## Lancement
```
.\run.bat        (Windows)
./run.sh         (Linux/Mac)
```

---

## Fonctionnement général

L'application est découpée en trois couches :
- **Métier** : les classes du graphe et les algorithmes (Dijkstra, Bellman-Ford)
- **Vue** : l'interface graphique (tableau de saisie, graphe visuel, terminaux)
- **Contrôleur** : fait le lien entre la vue et le métier

---

## Comment utiliser l'application

### Saisir le graphe (tableau)

Dans le tableau à gauche, saisir les arcs ligne par ligne :
- **Source** : le noeud de départ de l'arc (ex: A)
- **Destination** : le noeud d'arrivée de l'arc (ex: B)
- **Poids** : la valeur de l'arc (peut être négatif)

Boutons disponibles :
- `+ Ligne` : ajouter une ligne vide
- `- Ligne` : supprimer la ligne sélectionnée
- `Vider` : effacer tout le tableau

### Choisir l'algorithme

- **Dijkstra** : uniquement avec des poids positifs, plus rapide
- **Bellman-Ford** : gère les poids négatifs, plus lent

> Si un poids négatif est détecté dans le tableau, l'application bascule
> automatiquement sur Bellman-Ford et grise la combobox.

### Orienté / Non orienté

- **Coché** : les arcs sont à sens unique (A→B n'implique pas B→A)
- **Décoché** : chaque arc est automatiquement ajouté dans les deux sens
  (sauf si le poids est négatif — un arc retour négatif créerait
  automatiquement un circuit absorbant)

### Point de départ / Point d'arrivée

- **Point de départ** : obligatoire, c'est la source du calcul
- **Point d'arrivée** : optionnel (laisser "Aucun" pour tout calculer)
  - Avec point d'arrivée : affiche uniquement le chemin vers ce noeud
  - Sans point d'arrivée : affiche tous les chemins un par un avec
    le bouton **Chemin suivant**

### Visualisation du graphe

Après le calcul, le graphe s'affiche à droite :
- Les noeuds et arcs sont en gris par défaut
- Le bouton **Chemin suivant** colorie chaque chemin avec une couleur différente
- Les noeuds du chemin prennent aussi la couleur du chemin

---

## Options du menu

### Popup résultat
Affiche un tableau récapitulatif avec toutes les distances et chemins
dans une boîte de dialogue après le calcul.

### Terminal de saisie
Remplace le tableau par un terminal interactif.
Permet de saisir le graphe en mode texte, comme dans une console.
On peut l'utiliser en même temps que le terminal d'affichage.

### Terminal d'affichage
Ajoute un terminal en lecture seule à gauche de l'écran.
Il affiche le déroulé complet de l'algorithme après chaque calcul :
initialisation, itérations, relaxations, et détection de circuit absorbant.

---

## Algorithme de Dijkstra

### Principe
On part de la source avec une distance de 0. Tous les autres noeuds
sont à l'infini. À chaque étape on prend le noeud non traité avec
la plus petite distance connue, on regarde ses voisins et on met à
jour leurs distances si on trouve un chemin plus court.

Une fois un noeud traité, sa distance est définitive — on n'y revient plus.

### Pourquoi ça ne marche pas avec des poids négatifs
L'algorithme suppose que quand on sort un noeud de la file, sa distance
est optimale. Avec un poids négatif, un arc découvert plus tard pourrait
encore améliorer cette distance — l'hypothèse de base ne tient plus.

### Complexité
O(m * log(n)) avec une file de priorité, où n = noeuds et m = arcs.

---

## Algorithme de Bellman-Ford

### Principe
On répète **(n-1) fois** un passage sur **tous les arcs** du graphe.
À chaque passage on essaie de "relaxer" chaque arc : si passer par
cet arc donne une meilleure distance, on met à jour.

Pourquoi (n-1) fois ? Le chemin le plus long sans cycle a au maximum
(n-1) arcs. Donc après (n-1) passages, toutes les distances sont stables.

### Différence avec Dijkstra
Dijkstra choisit intelligemment le prochain noeud à traiter (le plus proche).
Bellman-Ford lui passe bêtement sur tous les arcs à chaque itération.
C'est moins efficace mais ça permet de gérer les poids négatifs.

### Complexité
O(n * m) où n = noeuds et m = arcs. Plus lent que Dijkstra.

---

## Détection des circuits absorbants

### Qu'est-ce qu'un circuit absorbant ?
C'est un cycle dans le graphe dont la **somme des poids est négative**.

Exemple : A→B→A avec poids 12 + (-48) = -36
On peut tourner en boucle dans ce cycle pour faire descendre la distance
vers -infini. Le problème du plus court chemin n'a donc pas de solution.

### Comment on le détecte
Après les (n-1) itérations de Bellman-Ford, on fait **un passage de plus**.
Si une distance peut encore être améliorée, c'est forcément à cause d'un
cycle négatif — sinon les distances auraient convergé.

### Comment on reconstruit le cycle
1. On note le noeud dont la distance a pu être améliorée lors du passage
   supplémentaire
2. On remonte n fois dans les prédécesseurs depuis ce noeud — ça garantit
   d'être à l'intérieur du cycle (et pas juste sur un chemin qui y mène)
3. On suit les prédécesseurs jusqu'à retomber sur ce même noeud
4. On affiche le cycle avec le détail des poids et leur somme

### Exemple affiché dans le terminal
```
Circuit absorbant détecté !
Cycle : A -> C -> E -> A
Somme des poids : (-48) + 5 + 6 = -37
=> Somme négative (-37) : le plus court chemin n'existe pas.
```

---

## Structure du projet
```
src/
├── metier/
│   ├── Arc.java          — un arc orienté avec son poids
│   ├── Noeud.java        — un sommet du graphe
│   ├── Graphe.java       — la structure du graphe (liste d'adjacence)
│   ├── Resultat.java     — le résultat d'un calcul + les logs
│   ├── Dijkstra.java     — algorithme de Dijkstra
│   ├── BellmanFord.java  — algorithme de Bellman-Ford
│   └── IAlgorithme.java  — interface commune aux deux algorithmes
├── controller/
│   └── Controller.java   — fait le lien vue <-> métier
└── vue/
    ├── Frame.java         — fenêtre principale
    ├── PanelTableau.java  — tableau de saisie des arcs
    ├── PanelGraphe.java   — visualisation graphique (GraphStream)
    ├── PanelTerminal.java — terminal texte (saisie ou affichage)
    └── VueConsole.java    — gestion des entrées/sorties console
```