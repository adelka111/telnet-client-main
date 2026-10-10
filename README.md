# Úkol 2 - Telnet Klient

[![UTB FAI Task](https://img.shields.io/badge/UTB_FAI-Task-yellow)](https://www.fai.utb.cz/)
[![Java](https://img.shields.io/badge/Java-007396.svg?logo=java&logoColor=white)](https://www.java.com/)
[![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?logo=gradle&logoColor=white)](https://gradle.org/)

|                                  |                                                          |
| -------------------------------- | -------------------------------------------------------- |
| __Maximální počet bodů__         | 4 bodů                                                   |
| __Způsob komunikace s aplikací__ | Parametry předané při spuštění + standardní vstup        |
| __Způsob testování aplikace__    | Na základě odesílaných zpráv po síti + standardní výstup |

---

## 📝 Zadání úkolu 

Implementujte telnet klienta, který vytvoří soket pro komunikaci s jiným telnet klientem na specifikované IP adrese a portu. Následně na tento soket bude posílat vše, co uživatel napíše na standartní vstup *System.in*. Na standartní výstup *System.out* bude vypisovat vše, co odpoví komunikující protistrana. Tento text vypisujte v nepozměněné formě. 

### Požadavky:
* Program bude přijímat __IP adresu a port__ jako parametry při spouštění aplikace.
* Zamezte blokování při operaci InputStream.read() pomocí volání InputStream.available().
Snižte zátěž CPU ve smyčce aktivního čekání pomocí volání Thread.sleep(20).
* Implementujte příjem a odesílání znaků ze Soketu v __nezávislých__ vláknech.
* Zpráva bude odeslána ve chvíli, kdy uživatel zmáčkne na klávesnici __ENTER__
* Pokud uživatel napíše __"/QUIT"__ a zmáčkne ENTER, aplikace ukončí spojení a ukončí se.

### 💡 Typ

> _Aplikaci telnet klienta můžete vyzkoušet například s využitím nástroje netcat, kdy následujícím příkazem na localhostu spustíte server, ke kterému se váš klient připojí. `nc -lv 127.0.0.1 4444`_

```java
package main.java.utb.fai;

import java.io.*;
import java.net.*;

public class TelnetClient {

    private String serverIp;
    private int port;

    public TelnetClient(String serverIp, int port) {
        this.serverIp = serverIp;
        this.port = port;
    }

    public void run() {
        try {
            Socket socket = new Socket(serverIp, port);
            // Implementation of receiving and sending data
            // Implement processing of input from the user and sending data to the server
            // Implement response processing from the server and output to the console
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

V hlavní třídě main implementujte zpracování vstupních parametrů, které jsou aplikaci předávány při spuštění.

### Struktura pole vstupních parametrů
1. __args[0]__ - IP adresa klienta, se kterým bude naše aplikace komunikovat _(String)_
2. __args[1]__ - Číslo portu, na kterém bude aplikace komunikovat. Standardně 23 pro telnet.  _(int)_

```java
package main.java.utb.fai;

public class App {

    public static void main(String[] args) {
        // TODO: Implement input parameter processing

        TelnetClient telnetClient = new TelnetClient("127.0.0.1", 23);
        telnetClient.run(); // run telnet client
    }
}
```

>_**Poznámka:** Implementace a struktura kódu aplikace je libovolná a je zcela na vás, jak tento problém vyřešíte. Je však důležité, aby aplikace splňovala zadané požadavky._

---

## 🏆 Způsob hodnocení

Vaše implementace bude hodnocena na základě chování aplikace při testování různých scénářů. Automatizovaný testovací nástroj bude předávat vaší aplikaci různé parametry, včetně platných a neplatných, aby otestoval její chování za různých podmínek. V případě testování síťové komunikace mezi více klienty, testovací nástroj bude vytvářet virtuální klienty/servery za účelem ověření funkcionality.

Výsledné hodnocení bude záviset na celkovém počtu úspěšných testovacích případů. Počet bodů získaných z úlohy bude tedy záviset na celkové úspěšnosti při testování. Váš výsledný počet bodů bude určen následujícím vzorcem.

__VP__ = __MB__ * (__UT__ / __CPT__)

### Popis symbolů:

* __VP:__ Výsledný počet bodů.
* __MB:__ Maximální počet bodů pro danou úlohu.
* __UT:__ Počet úspěšných testovacích případů.
* __CPT:__ Celkový počet testovacích případů.

## ⚙️ Jak spustit automatizované hodnocení lokálně na svém počítači?

Automatizované hodnocení můžete spustit lokálně za účelem ověření funkčnosti vaší aplikace. K tomu slouží předpřipravený skript, který je dostupný v repozitáři tohoto úkolu. Výsledný report testování se bude nacházet v souboru ```test_report.html```.

###  Pro uživatele systému Linux:
Spusťte skript s názvem ```run_local_test.sh```.

### Pro uživatele systému Windows:
Spusťte skript s názvem ```run_local_test.bat```.

