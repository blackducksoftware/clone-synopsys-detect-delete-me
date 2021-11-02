# Running the [solution_name] script

The primary function of the [solution_name] scripts is to download and execute the [solution_name] .jar file.
Several aspects of script functionality can be configured, including:

* The [solution_name] version to download/run; by default, the latest version.
* The download location.
* Where to find Java.

Information on how to configure the scripts is in [Shell script configuration](../../scripts/overview.md).

## Running the script on Linux or Mac

On Linux or Mac, execute the [solution_name] script ([bash_script_name], which is a Bash script) from Bash.

To download and run the latest version of [solution_name] in a single command:

````
bash <(curl -s -L https://detect.synopsys.com/detect7.sh)
````

Append any command line arguments to the end, separated by spaces. For example:

````
bash <(curl -s -L https://detect.synopsys.com/detect7.sh) --blackduck.url=https://blackduck.mydomain.com --blackduck.api.token=myaccesstoken
````

See [Quoting and escaping shell script arguments](../../scripts/script-escaping-special-characters.md) for details about quoting and escaping arguments.

### To run a specific version of [solution_name]:

````
export DETECT_LATEST_RELEASE_VERSION={[solution_name] version}
bash <(curl -s -L https://detect.synopsys.com/detect7.sh)
````

For example, to run [solution_name] version 5.5.0:

````
export DETECT_LATEST_RELEASE_VERSION=5.5.0
bash <(curl -s -L https://detect.synopsys.com/detect7.sh)
````

## Running the script on Windows

On Windows, execute the [solution_name] script ([powershell_script_name], which is a PowerShell script) from
the [Command Prompt](https://en.wikipedia.org/wiki/Cmd.exe).

To download and run the latest version of [solution_name] in a single command:

````
powershell "[Net.ServicePointManager]::SecurityProtocol = 'tls12'; irm https://detect.synopsys.com/detect7.ps1?$(Get-Random) | iex; detect"
````

Append any command line arguments to the end, separated by spaces. For example:

````
powershell "[Net.ServicePointManager]::SecurityProtocol = 'tls12'; irm https://detect.synopsys.com/detect7.ps1?$(Get-Random) | iex; detect" --blackduck.url=https://blackduck.mydomain.com --blackduck.api.token=myaccesstoken
````

### To run a specific version of [solution_name]:

````
set DETECT_LATEST_RELEASE_VERSION={[solution_name] version}
powershell "[Net.ServicePointManager]::SecurityProtocol = 'tls12'; irm https://detect.synopsys.com/detect7.ps1?$(Get-Random) | iex; detect"
````

For example, to run [solution_name] version 5.5.0:

````
set DETECT_LATEST_RELEASE_VERSION=5.5.0
powershell "[Net.ServicePointManager]::SecurityProtocol = 'tls12'; irm https://detect.synopsys.com/detect7.ps1?$(Get-Random) | iex; detect"
````

See [Quoting and escaping shell script arguments](../../scripts/script-escaping-special-characters.md) for details about quoting and escaping arguments.