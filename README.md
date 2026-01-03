# Kratak opis sistema
Ideja projekta jeste kreiranje smart home budilnik sistema. Fokus sistema biće na osiguravanju buđenja korisnika. Kako bi se u tome uspelo, sistem će posedovati visok stepen redundantnosti. Cilj sistema će takođe biti mogućnost prilagođavanja okruženju, ali i korisnikovim preferencijama. Kako bi se sprečio povratak spavanju nakon gašenja budilnika, sistem će koristiti temo kameru za detekciju korisnika u krevetu. 
<br/>

<img src="https://ftp.moma.rs/SleepTherminator/clock.png" width="640"/>

# Delovi sistema
Kako bi se svakom od elementa sistema omogućilo adekvatno pozicioniranje unutar prostorije, sistem će biti podeljen u module. Sistem će se sastojati od 3 modula: sata, kamere i zvučnika. Moduli će biti bežično povezani, a koordinisanje sistema će se vršiti u okviru sat modula.

![](https://ftp.moma.rs/SleepTherminator/nacrt.png)

## Kamera modul
Bitno je istaći da je rezolucija termo kamere svega 32x24 piksela. Pored toga potrebno je uzeti u obzir da će se subjekat nalaziti na znatnoj udaljenosti od kamere (oko 2 metra). Kao posledica ovoga, očekivana veličina subjekta biće svega nekoliko piksela, te će algoritam za detekciju biti relativno primitivan i ograničenog broja parametara (npr. threshold koji bi zavisio od temperature sobe).

![](https://ftp.moma.rs/SleepTherminator/demo.png)

### Komponente
- [x] MLX90640BAB termo kamera (BAB - variant with smaller FOV lens) 
- [x] Arduino Nano 
- [x] JDY-33 Bluetooth modul 
- [x] 5V napajanje 

## Zvučnik modul
Kako bi se sprečilo nasilno prigušivanje sata upotrebom obližnjih objekata (npr. jastukom), zvučnik bi bio odvojen kao zaseban bežični modul. Ovim se omogućava pozicioniranje zvučnika na teže dostižnim lokacijama. Još jedan od benefita je mogućnost korišćenja jačih zvučnika sa većom potrošnjom bez uticanja na trajanje baterija u centralnom sat modulu.

### Komponente 
- [x] Bluetooth audio receiver
- [x] PAM8610 2x15W pojačalo
- [x] 10W 8Ohm zvučnici
- [x] LM2596 step down DC-DC regulator
- [x] 12V napajanje

## Sat modul
Centralni koordinator u sistemu biće sat modul. U njemu će se odvijati procesiranje signala kamera modula, kao i upravljanje zvučnik modulom. Konfiguracije i pesme budilnika će se lokalno skladištiti u okviru sat modula. Osnovni elementi ovog modula su Raspberry Pi 3, dugme za gašenje budilnika i ekran za prikaz informacija kao što su trenutno vreme, vreme do sledećeg budilnika, vremenska prognoza itd… Kako bi se povećala redundantnost sistema, modul će takođe posedovati i USP za slučaj gubitka napajanja. Takođe će posedovati i sopstveni sistem zvučnika u slučaju da je zvučnik modul nedostupan.

### Komponente 
- [x] Raspberry Pi 3 model A+
- [x] Wisecoco 6.2 Inch 360x960 IPS Display SPI RGB Interface
- [x] QTSS HDMI-RGB21-V06 LCD Board for 40Pin display
- [x] Samoresetujući prekidač
- [x] PCM5102 I2S DAC
- [x] CJMCU-8406 pojačalo
- [x] 3W 4Ohm zvučnici
- [x] Waveshare UPS Module 3S
- [x] 3 Li-ion baterije 18650
- [x] 12V napajanje
- [ ] Kablovi i konektori


# Upravljanje sistemom
Sat modulom bi se pristupalo preko kućnog servera sa zakupljenim domenom (Raspberry Pi 4). Kućni server bi se ponašao kao reverse proxy između sat modula i klijentske aplikacije. Takođe, kućni server bi služio za hostovanje klijentske web aplikacije.

## Funkcionalni zahtevi

- Zakazivanje budilnika
    - Budilnik bi predstavljao sekvencu alarma sa početkom u određeno vreme
    - Za svaki alarm bi bilo moguće definisati vreme u odnosu na početak sekvence
    - Zvuk alarma može da se definiše i nasumičnim izborom iz korisnički definisanih listi
    - Moguće je podesiti vreme nakon kog se proverava da li se korisnik vratio u krevet
    - Moguće je podesiti jačinu zvuka alarma
- Podešavanje kamere
    - Pregled (preview) kamere zarad olakšanog procesa pozicioniranja i kalibrisanja
    - Podešavanje parametara detekcije (threshold-a)
- Podešavanje ekrana
    - Podešavanje osvetljenosti ekrana, fonta i boje teksta
    - Podešavanje aktivnog vremena ekrana
    - Podešavanje korisnih informacija kao što je vremenska prognoza
- Podešavanje baterije
    - Pregled trenutne napunjenosti baterije
    - Podešavanje upozorenja o nivou baterije


## Nefunkcionalni zahtevi

Sistemom bi se pretežno upravljalo telefonom, međutim potrebno je zadržati opciju upravljanja i sa drugih uređaja. Iz tog razloga, za izradu klijentske aplikacije bio bi korišćen Flutter, kako bi se omogućila cross platform kompatibilnost. Za kreiranje backend servera koristio bi se neki od python framework-a, kao što je Sanic.
