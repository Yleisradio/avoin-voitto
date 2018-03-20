# Lätkä-Voitto

Lätkä-Voitto on yksi Ylen kokeiluista automatisoida dataan perustuvaa journalismia. Se kirjoittaa suomen ja ruotsin kielillä jääkiekon tulos- ja tilastodatasta artikkeleita, joita julkaistaan [Yle Uutisvahdissa](https://yle.fi/uutisvahti/) ja Ylen [verkkosivuilla](http://haku.yle.fi/?q=voitto-robotti&sort=date).

Lätkä-Voiton koodi julkaistaan avoimena esimerkkinä automatisoidusta sisällöntuotannosta ja on käytettävissä [MIT-lisenssin](LICENSE) puitteissa.

Mestiksen ja Naisten Liigan datan tekijänoikeudet ovat Suomen Jääkiekkoliitolla.

--

Lätkä-Voitto is one of Yle's experiments to automate data-driven journalism. It writes articles in Finnish and Swedish based on ice-hockey scores and statistical data. The articles are published in [Yle Uutisvahti](https://yle.fi/uutisvahti/) and on Yle's [website](http://haku.yle.fi/?q=voitto-robotti&sort=date).

Lätkä-Voitto's code is an example of automated jorunalism and is open source under [MIT License](LICENSE).

Suomen Jääkiekkoliitto owns copyrights to Mestis and Naisten Liiga -data.


## Dependencies
- Java Runtime Environment
- [sbt](https://www.scala-sbt.org/)

## Running Locally

Starting the server is fairly straightforward:

- Install [sbt](https://www.scala-sbt.org/)

- Run the following command at the root of the git repo:

    `sbt run`
    
- The server should now be running locally and respond at http://localhost:45258/ping

- Fetch an article from local test data: http://localhost:45258/localReport/jkl-0-2018-3814

The program can be compiled using

    sbt compile

## Running Tests

To run all tests, run:

    sbt test

To continuously execute a single test during development, e.g. `LiigaJournalistSpec`, run:

    sbt ~'testOnly *LiigaJournalistSpec*' -mem 4096

**N.B. Most tests have been removed due to licensing issues regarding test data**

## API Description

Articles are identified using an id string, which is of the form:

    jkl-0-2018-<match-id>
    
Where `<match-id>` is the identifying number of the match. These numbers can be found, for example in [Tilastopalvelu](http://www.tilastopalvelu.fi/ih/beta/tilastointi/index.php/etsi#sarjat-ja-tilastot).

To fetch an article 

    http://localhost:45258/report/jkl-0-2018-3814

Select language with `lang` GET parameter:

    http://localhost:45258/report/jkl-0-2018-3814?lang=sv
    
To use local data, use "localReport" instead of "report":

    http://localhost:45258/localReport/jkl-0-2018-3814
    http://localhost:45258/localReport/jkl-0-2018-3814?lang=sv
    
PING (check if service is alive, responds ”PONG”):
    
    http://localhost:45258/ping

## Using local data

Instead of using Scores API, local data can be used. All game data from 2017 - 2018 series of Mestis and Naisten Liiga can be fetched from [here](https://static.cdn.yle.fi/10m/voitto/data_v1.zip). The match ids of the mestis games range from 3648 to 3947. For Naisten Liiga the ids range from 4951 to 5070. You can find ids for specific matches using [Tilastopalvelu](http://www.tilastopalvelu.fi/ih/beta/tilastointi/index.php/etsi#sarjat-ja-tilastot). By default Lätkä-Voitto has data for matches 3748 and 3814.

Data should be placed unzipped in the [data folder](data/).

## How does it work?

![Flowchart of Voitto robotti](doc/Voitto-diagram.png?raw=true "Voitto flowchart")

## Code

Lätkä-Voitto is written in Scala and all resources are in .edn format. 

Lätkä-Voitto fetches data from statistics and then converts the data into simple values. For example: `wonInOvertime` is `true` if the game was won in overtime. The values are used to determine what is worth mentioning in the article. 

Certain template texts are picked, which form the articles title, lead and body texts. Values are then placed in the templates to add details. For example: `firstGoalPlayer`, which is the name of the player who scored the first goal. 

### Templates

Templates contain all the text that Lätkä-Voitto outputs. Templates are lines of texts with attributes and weights. Attributes are used to add values from code to the text templates. These are generally player and team names, times and numbers. Attributes are marked in the templates between double brackets:

`{{attributeName}}`

Attributes are stored in a map/dictionary of attribute names and values. If no attribute is found for the template, the template will not be rendered. This includes attributes with missing declensions.

Weights can be used to affect the chance that a certain string is selected from the list. Default weight is 1.0. For example the following string is much less likely to occur (occurs only when all other templates fail to render. In this case, when teams don't have declensions defined.):

`["{{winner}} voitti kotonaan, vieraana {{loser}}" {:weight 0.01}]`

Templates are located in the directory [src/main/resources/template](src/main/resources/template)

Example code where templates are selected and values added to form body text:  

[src/main/scala/liigavoitto/journalist/body/GameProgressTexts.scala](src/main/scala/liigavoitto/journalist/body/GameProgressTexts.scala)

#### Translations
Translations are written directly into the same template files. A new language is added by adding a new translation to all templates. The keycode (e.g. `:fi` and `:sv`) used in the translation can then be used in the lang-parameter of the API (e.g. `?lang=fi` or `?lang=sv`). 

Example of template text translated to both Finnish and Swedish:

```edn
:home-team-first-place-win
{:fi
 (["Voiton myötä {{home}} vankisti asemiaan sarjan kärjessä"])
 :sv
 (["I och med segern stärkte {{home}} sin ställning i ledning av ligan"])}`
```
### Declensions

Attributes can have declensions. These are marked with colon and the name of the declension.
For example `{{loser:accusative}}` would contain the value ”loser” is in the accusative form. 

All declensions are listed in files located in directory: [src/main/resources/declension](src/main/resources/declension) 

[src/main/scala/liigavoitto/journalist/utils/TemplateUtils.scala](src/main/scala/liigavoitto/journalist/utils/TemplateUtils.scala) contains declension handling code

**N.B. All template strings should have a form without declensions so that articles can be written successfully even if no declension is found for the input.**

## Contributing

Bug reports and pull requests are welcome on GitHub at [https://github.com/Yleisradio/avoin-voitto](https://github.com/Yleisradio/avoin-voitto). This project is intended to be a safe, welcoming space for collaboration, and contributors are expected to adhere to the [Contributor Covenant](https://www.contributor-covenant.org/) code of conduct.

### Code Guidelines

- All code needs to have unit tests
- Code should be properly formatted
- Use functional programming paradigm

## Contact

Contact lassi.seppala@yle.fi or jarkko.ryynanen@yle.fi if you have ideas to pitch to Yle concerning Voitto-robot.

## License

Avoin-Voitto / Lätkä-Voitto is [MIT Licensed](LICENSE)

Mestis and Naisten Liiga -data is property of Suomen Jääkiekkoliitto

