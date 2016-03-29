
Boot Workflow
----

Personal project template based on Respo, Boot, ClojureScript, Cirru Sepal...

### Develop

Compile Cirru code into ClojureScript:

```bash
boot compile-cirru
```

Genetate HTML(`target/index.html`), watch and build ClojureScript:

```bash
boot dev
```

Generate HTML, compile ClojureSript:

```bash
boot build-simple
```

Generate HTML, compile and optimize ClojureScript:

```bash
boot build-advanced
```

Package jar file and install locally:

```bash
boot build
```

Package jar file and send to Clojars:

```bash
boot deploy
```

### License

MIT
