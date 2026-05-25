# AGENTS.md — Utilisation d'un agent IA

## Déclaration

Dans le cadre de ce projet, un agent IA (**Claude Code — Anthropic**) a été utilisé
pour m'accompagner dans la mise en place de l'architecture et de ma partie du code.

**Étudiant concerné :** Falandy Jean — Étudiant 1 (Architecte de Données & Services)

---

## Rôle de l'agent

L'agent a été utilisé pour :

1. **Générer l'arborescence complète du projet** (responsabilité de l'Étudiant 1
   selon le plan de répartition), avec l'architecture MVVM vide pour les 4 étudiants.
2. **Implémenter ma couche** : infrastructure réseau Retrofit/OkHttp, interface
   `PlaceApiService`, modèles POJO, et wrapper `ApiResult`.
3. **Créer des stubs documentés** pour les couches des autres étudiants
   (Room, ForegroundService, ViewModels, Fragments) avec des `TODO` explicites.
4. **Configurer la sécurité des clés API** via `local.properties` + `BuildConfig`
   pour un dépôt public GitHub.

Toute implémentation métier restante (Repository, ViewModels, UI, Maps, Room)
est réalisée par chaque étudiant concerné.

---

## Prompt fourni à l'agent

> Voici le plan de répartition de notre projet Android CityPulse, divisé en 4 étudiants.
> Je suis l'Étudiant 1 (Architecte de Données & Services), responsable de :
> - L'infrastructure réseau (Retrofit, OkHttp, Gson)
> - L'interface API (Overpass ou OpenTripMap)
> - Les modèles de données POJO
> - La gestion des erreurs (wrapper ApiResult)
> - La création complète de l'arborescence du projet (responsabilité supplémentaire)
>
> Le projet complet doit être dans un dossier `app/` à l'intérieur du repo.
>
> **Ce que j'attends de toi :**
> 1. Dis-moi comment aborder correctement ce projet (architecture MVVM, packages,
>    conventions Android).
> 2. Génère l'arborescence complète du projet Android avec :
>    - Ma couche (data/remote) entièrement implémentée
>    - Des stubs compilables pour les couches des 3 autres étudiants, avec des
>      TODO détaillés indiquant exactement ce qu'ils doivent faire
> 3. Configure Gradle, le Manifest, les ressources XML de base et la navigation.
> 4. Assure-toi que les clés API ne sont jamais committées (local.properties +
>    BuildConfig), avec un fichier local.properties.example comme guide.
>
> Je continuerai moi-même à intégrer ma partie une fois l'arborescence en place.

---

## Ce que l'agent n'a PAS fait

- Il n'a pas implémenté la logique métier des autres étudiants.
- Il n'a pas remplacé la compréhension du code par l'étudiant.
- Toute la partie réseau a été relue, comprise et validée par l'étudiant avant
  intégration.
## Logique Métier 
- J'ai utilisé l'IA pour m'aider à comprendre l'architecture MVVM et à structurer mes dossiers dans Android Studio. 
- L'IA m'a fourni les bases de code pour le Repository (gestion du mode hors-ligne), le ViewModel (calcul de distance et filtrage) et l'implémentation du partage via Intent implicite.

---

## Session 2 — Corrections et intégration Google Places

### Tâche 1 : Correction du crash onglet "Lieux"
**Instruction :** "lorsque je clique sur lieux, l'app se crashe"  
**Diagnostic :** `res/color/chip_text_color.xml` avait `<resources><selector>` — format invalide pour un color state list (le root doit être `<selector>` directement).  
**Correction :** suppression de la balise `<resources>` englobante.

### Tâche 2 : Intégration Google Places API (vrais lieux)
**Instruction :** utiliser une clé API Google pour afficher de vrais lieux géolocalisés.  
**Travail de l'agent :**
- Clé stockée dans `local.properties` + exposée via `BuildConfig` (jamais committée)
- Création `data/remote/GooglePlacesApi.kt`, `PlacesModels.kt`, `RetrofitClient.kt`
- `PlaceRepository` : fetch Nearby Search, mapping vers `Place`, nettoyage Room
- `CityViewModel` : méthode `loadNearbyPlaces(lat, lon)`
- `MapFragment` : déclenchement au premier fix GPS

### Tâche 3 : Mise à jour AGENTS.md
**Instruction :** compléter ce fichier avec les nouvelles tâches de la session.