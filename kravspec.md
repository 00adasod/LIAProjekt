## Beskriv väsentliga detaljer som projektet ska innehålla:

### Koncept
Lärportal med integration till AI-chattbot

### Målsättning
Skapa en Lärportal med bra användarvänlighet så att alla kan använda den intuitivt.

### Tydlig målgrupp
Kursansvariga och kursdeltagare med okänd teknisk kompetens

### Tidsplan
Se veckoplan i ***Högsbo Säljkonsult AB.docx***

### Säkerhetskrav
Entra ID används för autentisering, HTTPS

## Skriv korta användarberättelser/user stories

### User Stories
- Användare ska kunna logga in och få tillgång till sin användarroll
- Kursansvarig kan ladda upp material och tester
- Kursansvarig ska kunna ladda upp video som deltagare kan se.
- Deltagare kan komma åt material för en del, och utföra test på den delen
- Deltagare kommer inte åt nästa del utan klarat test på tidigare del
- När deltagare klarar en del så ökas en progressindikator procentuellt till hur många delar det finns.
- E-postnotifieringar vid registrering, avklarat test och slutförd kurs
- Admin kan CRUD för AI-personas
- Deltagare ska kunna ställa frågor till en/flera AI-persona kopplade till kursen

## Beskriv alla väsentliga funktioner utifrån företagets behov 
Gallra och prioritera bland önskade funktioner.
Funktionalitet ökar komplexiteten i applikationen eller hemsidan, är ofta tidskrävande vilket gör projektet dyrare.
Jag rekommenderar att man utvecklar de högst prioriterade funktionerna i första versionen och lägger till fler funktioner efter hand i kommande versioner.

### Grundfunktioner
- Inloggning med roller: administratör, kursansvarig och kursdeltagare
- Kursadministration: skapa och redigera kurser, avsnitt och material
- Stöd för textbaserat material och inbäddade videofilmer
- Test efter varje avsnitt – deltagaren måste klara testet före upplåsning av nästa avsnitt
- Progressionsspårning per deltagare och kurs
- E-postnotifieringar vid registrering, avklarat test och slutförd kurs

### AI-funktioner
- Chattgränssnitt där deltagaren kan ställa frågor till fiktiva AI-drivna karaktärer kopplade till respektive kurs
- Karaktärerna svarar kontextuellt baserat på kursens innehåll via LLMintegration
  (t.ex. Azure OpenAI eller OpenAI API)
- Valfri utvidgning: röstinmatning(speech-to-text) och talsvar (text- tospeech) för tillgängligare interaktion
- Promptdesign för att styra karaktärernas personlighet, kunskapsområde och gränser


## Data och databas
Vilka krav och behov finns det?
Kommer information/data att samlas in och lagras i en databas?
Ska det skickas ut automatiska meddelanden? Ska databasen kopplas mot externa system?

Systemet kommer att samla in och lagra information om användare, kurser, kursmaterial, tester, progression samt notifieringar och kommunikation mellan användare och AI-funktioner. 
Denna information lagras i en databas för att säkerställa att data hanteras strukturerat, säkert och tillförlitligt sätt.

Databasen ska stödja hantering och uppdatering av information, exempelvis vid registrering av deltagare, registrering av testresultat och uppföljning av progression i kurser, 
Systemet ska även kunna generera automatiska meddelanden, såsom e-postnotifieringar vid registrering, avklarat test eller slutförd kurs, baserat på händelser som registreras i databasen.

Systemet kan komma att kopplas till externa tjänster, exempelvis  för e-postutskick eller AI-funktionalitet via externa API. 
En relationsdatabas, exempelvis PostgreSQL, väljs som databaslösning eftersom den lämpar sig väl för ett system med tydliga relationer mellan data och krav på hög dataintegritet och stabilitet.

## Administrationspanel 

### Vad ska kunna administreras på hemsidan/i appen?
Skapa, redigera och ta bort kursmaterial och koppla AI-personas med AI-chatt till kurser.

### Ska olika personer ha olika behörighet?
Kursdeltagare, kursansvarig och administratör ska ha olika behörigheter

### Finns det önskemål kring användardokumentation och utbildning i administrationspanelen?

## Gemensam kravspecifikation 
Skriv gemensam kravspec med moln-studenterna.
- Definiera API-kontrakt: autentiseringsflöde, dataformat, endpoints.
- Planera spårbarhet - hur kopplas krav till testfall?

### dataformat
markdown, json? (ai-personas)

### autentiseringsflöde
Entra ID

### Endpoints
- /
- /user
- /course
- /course/section
- /course/section/test
- /aicharacters
- /aiconversation - stateful API för chattbot
- /login - ??? kommer man via redirect direkt till entra id login eller ska man ha access utan auth för att kunna logga in här?