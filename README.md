# lead-graphite-server

Let Graphite query your data.

## Usage

Configure a [Lead](https://github.com/also/lead) server with a dependency on

```
[com.ryanberdeen/lead-graphite-server "0.1.0-SNAPSHOT"]
```

In your lead settings file, add the API handler:

```clojure
(require 'lead.graphite.api)
(add-routes lead.graphite.api/handler)
```

Graphite requires that you expose Lead over HTTP at `/`; using `set-uri-prefix` or HTTPS won't work.

Add your Lead server to Graphite's `CLUSTER_SERVERS` list. In `local_settings.py`:

```python
CLUSTER_SERVERS = ['lead-host:port']
```

See "[Cluster Configuration](http://graphite.readthedocs.org/en/latest/config-local-settings.html#cluster-configuration)" in the Graphite documentation for more details on Graphite settings.

## Compatibility

Compatible with Graphite versions

* 0.9.7c
* 0.9.8
* 0.9.9
* 0.9.10
* 0.9.12

Graphite versions 0.9.6 and 0.9.11 are broken (https://bugs.launchpad.net/graphite/+bug/595652, http://graphite.readthedocs.org/en/0.9.12/releases/0_9_11.html) and don't support `CLUSTER_SERVERS` without a patch.
