# EQUELLA

## Prerequisites for building

* [Ant](https://ant.apache.org)
* `equella-deps.zip` unzipped into your home directory
* `equella.keystore` in your home directory. This is the keystore containing the certificate for signing the webstart apps and applets.

## Building

```bash
ant release
```

The build artifacts will be produced in the `product` folder.
