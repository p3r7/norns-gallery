# norns-index


## Setup: ClojureScript / Javascript

This code is written in [ClojureScript](https://clojurescript.org/) that gets transpileld into Javascript.

For tooling, it uses [shadow-cljs](https://github.com/thheller/shadow-cljs) + [leiningen](https://leiningen.org/).

To run it:
- install node.js (preferably through [nvm](https://github.com/nvm-sh/nvm))
- install a JDK (Java 11 recommanded)
- install Clojure (latest version)
- install leiningen ([instructions](https://leiningen.org/#install))

Once those dependencies are satisfied, run:

    $ lein dev

This will retrieve all dependencies and launch an HTML server serving the page.

Open a web browser at the advertised URL.

Changes to the code are automatically detected and hot-swapped in the running browser session.


## Setup: CSS

The project relies on [tailwindcss](https://tailwindcss.com/).

In addition, custom styles are defined under [tw/style.css](./tw/style.css).

For changes made to this latter file to be taken into account, run:

    $ npm run tw

One could also run the following to enable hot-reload:

    $ npm run watch:css

All the HTML is generated from (functional) react components in [norns-index.views](./src/cljs/norns_index/views.cljs) written using the [hiccup](https://github.com/weavejester/hiccup) syntax (akin to JSX for Clojure(Script)).
