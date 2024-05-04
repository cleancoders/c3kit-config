# Config

Just an atom with a map.

## Why?

Modules often need configuration.  Where should the configuration parameters be stored?

One solution is to store configuration parameters in an atom in the module. That's great.  But what happens when you reload the module namespace?  All your parameters get wiped.  
The happens when you use `:override-deps` with `:local/root` in a reloading dev environment.  `c3kit.apron.refresh` offers that ability.

`c3kit.onfig` is just an atom with a map.  There's no reason to add it to `:override-deps` with `:local/root`.  So it'll never get reloaded.  Any configurations saved here will stay here.

## But that's Global State!

Yup. Exactly. Use wisely.
