
package zz_java_playground;



public class ConsoleExample {

  // {"tickers":[{"text":"Treibt es auf die Spitze: Reinhold Messner will nochmal Achttausender besteigen","short":"ste"},{"text":"Geht durch Mark und Bein: Pathologe f\u00fchrt neue Knochens\u00e4ge vor","short":"aua"},{"text":"Bremsen kaputt: Autofahrer rast in Stechm\u00fcckenschwarm","short":"tim"},{"text":"Glanzparade: Festlicher Umzug zu Ehren von Torwart durchgef\u00fchrt","short":"bor"},{"text":"Immer nur Blasen: Ballerinas zu eng","short":"jic"},{"text":"Biss Mark: Hering","short":"ind"},{"text":"Das sieht ihm \u00e4hnlich: Ehrgeiziger Gentechniker bastelt sich Klon-Kind","short":"tim"},{"text":"Ihr Mann ist Forscher: Aff\u00e4re mit Laborassistent unbefriedigend","short":"wst"},{"text":"Schinkenspicker: Sechstkl\u00e4sser schreibt L\u00f6sungen f\u00fcr Klassenarbeit auf Wurstscheibe","short":"met"},{"text":"Epidemie: Hunderte in M\u00fclheim an der Ruhr erkrankt","short":"n.n."},{"text":"Nicht mehr alle Tassen im Schrank: Porzellanh\u00e4ndler wird nach Gro\u00dfdiebstahl wahnsinning","short":"mbr"},{"text":"\u00dcbel mitgespielt: Kind erbricht in Sandkasten","short":"name"},{"text":"Scheint gut zu Arbeiten: Werkstattlampe funktioniert einwandfrei","short":"osr"},{"text":"Hervorragend: Dicke Beule","short":"sid"},{"text":"\"Todesfallen\": Gesundheitsminister l\u00e4sst Krankenh\u00e4user und Altenheime schlie\u00dfen","short":"ssi","link":"http:\/\/www.der-postillon.com\/2011\/02\/todesfallen-gesundheitsministerium.html "},{"text":"Nachgehwiesen: Stalker lieben gro\u00dfe Rasenfl\u00e4chen","short":"chj"},{"text":"Mit der Sauglocke geholt: Ferkel wurde durch Klingeln aus dem Geburtskanal gelockt","short":"mvp"},{"text":"Hat sich zu viel versprochen: Botschafter entt\u00e4uscht \u00fcber neuen Dolmetscher","short":"?"},{"text":"Hat seinem Chef einen B\u00e4rendienst erwiesen: Imkerlehrling nascht zu viel","short":"vgj"},{"text":"Rebellische Jugendliche versto\u00dfen immer h\u00e4ufiger gegen Gesetz der Schwerkraft","short":"ssi","link":"http:\/\/www.der-postillon.com\/2011\/05\/rebellische-jugendliche-verstoen-immer.html "}]}
  public static void main(String[] args) {
    
    String testStr = "Geht durch Mark &amp; Bein: \\\"Pathologe ffchrt neue Knochensge vor\\\""; 
    System.out.print(testStr+"\n");
    
    String testStr2 = testStr.replaceAll("\\\\\"", "\"");
    System.out.print(testStr2+"\n");
    
    testStr = testStr2.replaceAll("&amp;", "&");
    System.out.print(testStr+"\n");
  }

}
