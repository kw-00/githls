# GitHub LS
Aplikacja stanowi prosty serwer udostępniający REST API do interakcji z GitHub-em.

Aktualnie udostępniona jest jeden endpoint, który udostępnia informacje na temat
repozytoriów danego użytkownika, zgodnie z formatem opisanym niżej.

Format zapytań:

`<adres serwera>/<nazwa użytkownika na githubie>`

W przypadku lokalnego uruchomienia aplikacji:
`http://localhost:8080/<nazwa użytkownika>`

Format odpowiedzi:
```json
[
  {
    "name": "repositoryX",
    "ownerLogin": "Krzysztof",
    "branches": [
      {
        "name": "main",
        "lastCommitSha": "42e2e5a"
      },
      ...
    ]
  },
  ...
]
```

## Jak zbudować aplikację
Wymagana jest Java 25.
Należy otworzyć terminal z głównego katalogu projektu.

Następnie, należy użyć poniższej komendy:

`./gradlew build`

Następnie, aby uruchomić aplikację lokalnie, można użyć następującej komendy:

`./gradlew bootRun`

## Krótko o implementacji
Aplikacja udostępnia informacje na temat repozytoriów, do których posiada dostęp.

Wykorzystuje ona wirtualne wątki (Project Loom) wraz z kompatybilnymi bibliotekami Javy,
by ograniczyć blokowanie wątków systemowych.

Z uwagi na wymagania zadania, nie wprowadzałem żadnej formy autoryzacji. Zamiast WebFluxa,
do wysyłania zapytań wykorzystałem `java.net.http`.

Wszystkie klasy aplikacji znajdują się w jednym pakiecie.
