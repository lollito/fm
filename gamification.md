# Gamification & Engagement Strategy

Questo documento delinea la strategia di gamification per Football Manager, con l'obiettivo di aumentare la retention degli utenti, incentivare l'attività giornaliera e fornire un senso di progressione a lungo termine.

## 1. Visione e Obiettivi

L'obiettivo principale è trasformare l'esperienza di gestione da una serie di compiti amministrativi a un percorso di crescita personale del Manager (Utente).
*   **Retention:** Creare abitudini giornaliere tramite missioni e bonus login.
*   **Progressione:** Dare un senso di crescita oltre al semplice miglioramento della squadra (Leveling del Manager).
*   **Engagement:** Premiare comportamenti specifici (scouting approfondito, gestione finanziaria oculata).

---

## 2. Sistema di Progressione del Manager (Manager Level)

Ogni utente avrà un **Livello Manager** basato sull'esperienza (XP) accumulata.

### Fonti di XP (Esperienza)
Le azioni di gioco conferiscono XP. Esempi:
*   **Partita Giocata:** +50 XP
*   **Partita Vinta:** +100 XP (Bonus +20 XP per Clean Sheet)
*   **Allenamento Completato:** +10 XP
*   **Sessione Scouting Terminata:** +15 XP
*   **Promozione in Campionato:** +1000 XP
*   **Giocatore Venduto con Plusvalenza:** +5 XP per ogni 100k di profitto.

### Albero dei Talenti (Manager Perks)
Salendo di livello, il manager ottiene **Punti Talento** da spendere in tre rami principali:

1.  **Tattico (Tactical):**
    *   *Livello 1:* "Analista Video" -> +5% velocità apprendimento tattica.
    *   *Livello 5:* "Motivatore" -> Recupero Morale +10% dopo una sconfitta.
    *   *Livello 10:* "Fortezza" -> Bonus 'Home Advantage' aumentato del 2%.

2.  **Finanziario (Financial):**
    *   *Livello 1:* "Contrattatore" -> -5% richieste stipendio giocatori.
    *   *Livello 5:* "Marketing Guru" -> +5% entrate sponsor.
    *   *Livello 10:* "Investitore" -> Interessi sui depositi bancari.

3.  **Osservatore (Scouting):**
    *   *Livello 1:* "Occhio di Falco" -> Riduce il tempo di scouting del 10%.
    *   *Livello 5:* "Rete Globale" -> Aumenta la probabilità di trovare 'Wonderkids'.
    *   *Livello 10:* "Persuasore" -> Aumenta la probabilità che un giocatore accetti di trattare.

---

## 3. Sistema di Obiettivi (Quests)

Per mantenere l'engagement alto, viene introdotto un sistema di missioni cicliche.

### Missioni Giornaliere (Daily Quests)
*Reset ogni 24 ore (es. 00:00 UTC).*
*   "Gioca 3 partite."
*   "Completa una sessione di allenamento con Focus: Attacco."
*   "Osserva un giocatore sotto i 21 anni."
*   **Ricompensa:** XP, piccola somma di denaro in-game.

### Missioni Settimanali (Weekly Challenges)
*Reset ogni Lunedì.*
*   "Segna almeno 10 gol in partite ufficiali."
*   "Mantieni la porta inviolata per 3 partite."
*   "Ottieni 6 punti in campionato."
*   **Ricompensa:** Pacchetti risorse (es. consumabili per recupero condizione), XP medi.

### Obiettivi Stagionali (Season Pass)
Legati alla durata della stagione di gioco (es. 1 mese reale o durata del campionato).
*   Livelli da 1 a 50.
*   Si avanza ottenendo "Punti Stagione" dalle missioni giornaliere/settimanali.
*   **Ricompense Premium:** Skin per lo stadio, badge esclusivi per il profilo, titoli (es. "Il Tattico").

---

## 4. Achievements e Trofei

Obiettivi una tantum che celebrano pietre miliari della carriera.

### Categorie
*   **Legacy:** "Vinci il Campionato", "Vinci la Coppa", "Promozione".
*   **Finanza:** "Tycoon" (Accumula 100M in cassa), "Scrooge" (Non spendere nulla per una stagione).
*   **Match:** "Goleador" (Vinci 5-0), "Ribaltone" (Vinci rimontando da 0-2).
*   **Sviluppo:** "Academy Hero" (Fai esordire un giovane del vivaio e fallo segnare), "Trader" (Compra a X e rivendi a 3X).

**Visualizzazione:** Una bacheca trofei nel profilo allenatore visibile agli altri utenti.

---

## 5. Ricompense ed Economia

Oltre ai soldi del club (che servono per la gestione), introduciamo una valuta "Personale" o "Punti Prestigio" (PP).

*   **Valuta Club (€):** Usata per stipendi, trasferimenti, stadio.
*   **Punti Prestigio (PP):** Guadagnati tramite Achievements e Level Up.
    *   **Uso:** Sbloccare slot tattici extra (se limitati), cambiare nome al club, personalizzazioni estetiche (colore menu, logo club premium), "Mentoraggio Speciale" (evento una tantum per un giovane).

*Nota: I bonus non devono mai essere "Pay-to-Win" diretti che rompono il bilanciamento delle partite (es. "Paga per vincere la partita" è vietato).*

---

## 6. Social e Competizione

*   **Leaderboards:**
    *   Classifica Globale Manager (basata su XP o Rating ELO).
    *   "Miglior Attacco della Settimana".
    *   "Club più Ricco".
*   **Rivalità:** Il sistema identifica club con cui si giocano spesso partite decisive e crea una "Rivalità" che dà bonus XP extra per le vittorie negli scontri diretti.

---

## 7. Implementazione Tecnica

### Nuove Entità (Database)

1.  **`ManagerProfile`** (OneToOne con `User`)
    *   `level` (int)
    *   `currentXp` (long)
    *   `talentPoints` (int)
    *   `talents` (JSON/List: lista talenti sbloccati)

2.  **`Achievement`**
    *   `id` (String/Enum): Codice univoco (es. `WIN_LEAGUE_1`)
    *   `name` (String)
    *   `description` (String)
    *   `category` (Enum)
    *   `xpReward` (int)

3.  **`UserAchievement`**
    *   `userId`
    *   `achievementId`
    *   `unlockedAt` (Timestamp)

4.  **`Quest` / `DailyMission`**
    *   `userId`
    *   `type` (Enum: `PLAY_MATCH`, `TRAIN`, etc.)
    *   `targetValue` (int)
    *   `currentValue` (int)
    *   `status` (Enum: `ACTIVE`, `COMPLETED`, `CLAIMED`)
    *   `expirationDate` (Timestamp)

### Architettura Event-Driven
Utilizzare Spring Events per disaccoppiare la logica di gioco dalla gamification.

*   **Events:** `MatchFinishedEvent`, `TrainingCompletedEvent`, `TransferCompletedEvent`.
*   **Listeners:** `GamificationEventListener`.
    *   Ascolta `MatchFinishedEvent`.
    *   Calcola XP guadagnati.
    *   Aggiorna `ManagerProfile`.
    *   Controlla progresso `DailyMission`.
    *   Controlla sblocco `UserAchievement` (es. "Vinci 5 partite di fila").
    *   Invia notifica WebSocket al frontend ("Level Up!", "Missione Completata").

### Frontend
*   Nuova dashboard "Carriera Allenatore".
*   Widget nella Home per "Missioni del Giorno".
*   Popup/Toast notification per achievement sbloccati.
