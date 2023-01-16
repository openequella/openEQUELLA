# Background about JSVC and oEQ

JSVC is a binary required to support running oEQ and the Upgrade Manager as a Daemon process in
Unix alike systems. It had not been updated until 2022 when we started adding the support
for Java 11. And the previous version of JSVC used in oEQ does not support Java 11. As a result,
we tried to build the JSVC binary again.

## How to build the JSVC binary
* Install required dependencies
  * GNU AutoConf (at least version 2.53)
  * An ANSI-C compliant compiler (e.g GCC)
  * GNU Make
* Download the source code from this [page](https://commons.apache.org/proper/commons-daemon/download_daemon.cgi)
* Unzip the file and go to directory `src/native/unix `
* Execute the provided scripts to generate the binary
    ```
      ./support/buildconf.sh
      ./configure
    ```
* You will notice some files are created after above step. And now you can run `make` to generate the executable file.

For more details, please check this [page](https://commons.apache.org/proper/commons-daemon/jsvc.html)

## How to get the Windows executables
The JSVC equivalent on Windows is called `Procrun` which typically has two executables: `prunmgr.exe` and `prunsrv.exe`.

These two files can be downloaded from this [page](https://archive.apache.org/dist/commons/daemon/binaries/windows/).

For details about how to use the executables, please check this [page](https://commons.apache.org/proper/commons-daemon/procrun.html).
