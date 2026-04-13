## Välj ORM.
Spring Data JPA med Hibernate (Motivera)
(Boolean i Java blir konverterat till BIT för Azure SQL)

## Designa testmotorns låslogik.

### Regler för test och upplåsning:
- Ett avsnitt låses upp först när föregående test resultat är godkänt (om inte första avsnitt)
- Test kan göras om vid underkänt resultat
- Resultatet sparas automatiskt när testet avslutas
- Godkänt resultat defineras som minst X procent rätt

### Om användaren stänger webbläsaren:
- Teststatus sparas som "pågående"
- Testet kan återupptas vid nästa inloggning
- inga resultat registreras föränn alla frågar har blivit svarade

### Vid avklarat test
- Antal rätt svar räknas ihop och kontrolleras om godkänt
- Vid godkänt så ändras boolean "passed" till true

## Skissa promptdesign för AI-karaktärerna
````
"Du är en AI-assistent som svarar enbart baserat på tillhandahållet kursmaterial och en tillhörande persona-beskrivning.
Regler:
- Du får endast använda information som finns i kursmaterialet.
- Om information saknas ska du svara: "Detta finns inte i kursmaterialet."
- Du får inte använda extern kunskap eller gissningar.
- Du ska följa den persona som ges i markdown-filen.
- Du ska svara pedagogiskt och tydligt.

Om användaren frågar något som inte finns i materialet:
- Svara kort.
- Försök inte resonera vidare.

" + persona.md + kursmaterial 
````
Hur gör vi om kursmaterial är en video och inte powerpoint?