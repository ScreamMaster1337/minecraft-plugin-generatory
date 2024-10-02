# SkyPVP Generator Plugin

SkyPVP Generator to plugin do Minecrafta, który umożliwia tworzenie generatorów w trybie SkyPVP. Generatory pozwalają na automatyczne tworzenie zasobów w określonym czasie.

## Funkcje
- **Tworzenie generatorów**: Utwórz generator, który będzie automatycznie generował zasoby co określony czas.
- **Usuwanie generatorów**: Usuń niepotrzebne generatory.
- **Lista generatorów**: Wyświetl aktualnie dostępne generatory na serwerze.
- **Przeładowanie konfiguracji**: Przeładuj konfigurację pluginu bez konieczności restartowania serwera.

## Użycie Komend

- `/generator create <czas>`  
  **Opis:** Tworzy nowy generator, który będzie generował zasoby co określoną ilość sekund.  
  **Przykład:** `/generator create 60s` – Tworzy generator, który co 60 sekund generuje zasoby.

- `/generator delete <id>`  
  **Opis:** Usuwa generator o określonym ID.  
  **Przykład:** `/generator delete 1` – Usuwa generator o ID 1.

- `/generator reload`  
  **Opis:** Przeładowuje konfigurację pluginu.

- `/generator list`  
  **Opis:** Wyświetla listę wszystkich aktualnie istniejących generatorów.

## Konfiguracja

Po instalacji, plugin automatycznie utworzy plik konfiguracyjny, w którym możesz dostosować ustawienia generatorów.

---

**Wymagania:**
- Serwer Minecraft (wersja 1.16+)
- Java 8+

**Autor:** [ScreamMaster1337](https://github.com/ScreamMaster1337)  
**Licencja:** MIT
