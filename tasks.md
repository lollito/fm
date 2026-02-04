# Task di Progetto: Football Manager

In questo documento sono elencati i task necessari per completare il progetto e portarlo in produzione, suddivisi per User Stories.

## User Story 1: Come Manager, voglio gestire la mia squadra in modo completo
- [ ] **Sistema di Infortuni**: Implementare infortuni casuali durante i match o l'allenamento, con relativi tempi di recupero e impatto sulle prestazioni.
- [ ] **Allenamento Personalizzato**: Permettere al manager di impostare focus di allenamento settimanali per migliorare specifiche abilità dei giocatori (es. attacco, difesa, stamina).
- [ ] **Settore Giovanile**: Implementare la generazione periodica di nuovi talenti ("regen") che possono essere promossi in prima squadra.
- [ ] **Gestione dello Staff**: Aggiungere la possibilità di assumere e licenziare staff (allenatori, medici, osservatori) che forniscono bonus alle prestazioni o al recupero.
- [ ] **Storico Giocatore**: Tracciare le statistiche di carriera (gol, presenze, media voto) per ogni stagione disputata.

## User Story 2: Come Manager, voglio operare nel mercato trasferimenti
- [ ] **Negoziazione Contrattuale**: Implementare una gestione più profonda dei contratti (durata, clausole rescissorie, bonus firma, bonus gol).
- [ ] **Sistema di Scouting**: Introdurre la necessità di "osservare" i giocatori di altri club per rivelarne i valori esatti delle abilità.
- [ ] **Lista dei Desideri (Watchlist)**: Permettere di seguire i giocatori interessanti e ricevere notifiche sulle loro prestazioni o cambiamenti di stato (es. messi in vendita).
- [ ] **Prestiti**: Gestire il trasferimento temporaneo di giocatori tra club con eventuale suddivisione dello stipendio.

## User Story 3: Come Manager, voglio gestire le finanze e le infrastrutture
- [ ] **Bilancio Dettagliato**: Creare una pagina dedicata alle finanze con il riepilogo di entrate (sponsor, biglietti, premi) e uscite (stipendi, manutenzione, mercato).
- [ ] **Sponsorizzazioni Dinamiche**: Implementare offerte di sponsor che variano in base al prestigio del club e ai risultati ottenuti.
- [ ] **Miglioramento Infrastrutture**: Permettere l'upgrade non solo dello stadio (capacità), ma anche del centro sportivo (bonus allenamento) e della clinica (bonus recupero infortuni).

## User Story 4: Come Manager, voglio vivere l'esperienza della partita in modo immersivo
- [ ] **Live Match Viewer**: Implementare una pagina di "cronaca in diretta" che mostra gli eventi del match man mano che accadono.
- [ ] **Cambi Tattici in Tempo Reale**: Permettere di effettuare sostituzioni o cambiare mentalità della squadra durante la simulazione della partita live.
- [ ] **Fattori Esterni**: Introdurre variabili come il meteo o le condizioni del campo che influenzano la probabilità di successo delle azioni.

## User Story 5: Come Amministratore, voglio gestire il mondo di gioco
- [ ] **Pannello di Controllo Admin**: Implementare in `fm-admin` strumenti per creare/modificare/eliminare campionati, club e giocatori manualmente.
- [ ] **Gestione Utenti**: Dashboard per la moderazione degli utenti, reset password e visualizzazione log di sistema.
- [ ] **Tool di Debug**: Possibilità di forzare l'avanzamento dei turni o simulare match specifici per testare il bilanciamento.

## User Story 6: Come Sviluppatore, voglio che l'applicazione sia robusta e pronta per la produzione
- [ ] **Internazionalizzazione (i18n)**: Supporto multilingua (Italiano/Inglese) su entrambi i frontend e messaggi di errore localizzati nel backend.
- [ ] **Gestione Errori e Notifiche**: Implementazione di un sistema globale di Toast/Notifiche per feedback immediato all'utente.
- [ ] **Sicurezza Avanzata**: Implementazione del recupero password tramite email e protezione contro attacchi comuni (Rate Limiting, XSS).
- [ ] **Caching**: Utilizzo di Redis per velocizzare le query frequenti (es. classifiche, profili giocatori).
- [ ] **Dockerizzazione**: Creazione di un file `docker-compose.yml` per avviare l'intero stack (Backend, Frontend, DB, Redis) in un unico comando.
- [ ] **CI/CD**: Configurazione di pipeline (es. GitHub Actions) per il testing automatico e il deploy su ambienti di staging/produzione.
- [ ] **Monitoraggio**: Integrazione di strumenti come Spring Boot Actuator e Prometheus/Grafana per monitorare la salute e le performance del sistema.
