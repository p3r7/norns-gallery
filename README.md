# norns-index


## Implentation Notes

- http://localhost:8666
- http://localhost:8666/?random=4
- http://localhost:8666/?category=art
- http://localhost:8666/?connectivity=jf
- http://localhost:8666/?author=infinitedigits


## Setup: ClojureScript / Javascript

This code is written in [ClojureScript](https://clojurescript.org/) that gets transpiled into Javascript.

For tooling, it uses [shadow-cljs](https://github.com/thheller/shadow-cljs) + [leiningen](https://leiningen.org/).

To run it:
- install node.js (preferably through [nvm](https://github.com/nvm-sh/nvm))
- install a JDK (Java 11 recommanded)
- install Clojure (latest version)
- install leiningen ([instructions](https://leiningen.org/#install))

Grab js dependencies:

    $ npm install

(this is supposed to be automaticly done by `shadow-cljs` but for some reason is buggy)

And finally launch the app to grab the last dependencies:

    $ lein dev

This will retrieve all dependencies and launch an HTML server serving the page.

Open a web browser at the advertised URL.

Changes to the code are automatically detected and hot-swapped in the running browser session.


## Setup: CSS

Compile SASS with:

    $ sass sass/main.sass:resources/public/css/main.css

Alternatively, to auto-compile on source change:

    $ npm run watch:css

All the HTML is generated from (functional) react components in [norns-index.views](./src/cljs/norns_index/views.cljs) written using the [hiccup](https://github.com/weavejester/hiccup) syntax (akin to JSX for Clojure(Script)).


## Deploying

Re compile everything w/ PROD target:

    $ sass sass/main.sass:resources/public/css/main.css
    $ lein prod

Copy content of `resources/public/` folder to a local clone of [norns-gallery-render](https://github.com/p3r7/norns-gallery-render).

Commit and push.

The published website is available at: https://p3r7.github.io/norns-gallery-render/


### Static Site generator

Just run:

    $ lein cljsbuild once prerender
    $ node target/cljsbuild/prerender/main.js --mode dir --dest-dir target/cljsbuild/prod-static/public/

The way it works is by running the script [prerender.cljs](./prerender/cljs/norns_index/prerender.cljs) which converts the selected [norns-index.views](./src/cljs/norns_index/views.cljs) into static HTML pages, using react/reagent server-side rendering ([doc](https://reagent-project.github.io/docs/master/reagent.dom.server.html)) through a node.js runtime.

This approach is inspired by the [reagent prerender example](https://github.com/reagent-project/reagent/blob/master/prerender/sitetools/prerender.cljs).

An alternative approach would have been to convert the views into cljc and do the rendering using a clj script w/ [hiccup](https://github.com/weavejester/hiccup) instead of reagent ([article](https://yogthos.net/posts/2015-11-24-Serverside-Reagent.html)).

A final alternative would be to keep the script in cljs and have it run under clj w/ GraalVM & Polyglot ([article](https://www.arthurbrrs.me/prerendering-react-clojurescript-land)).
