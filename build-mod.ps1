param([switch]$Client, [switch]$Server)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$jdk25 = Get-ChildItem "$PSScriptRoot/.tools/java25" -Directory | Select-Object -First 1
$jdk21 = Get-ChildItem "$PSScriptRoot/.tools/java" -Directory | Select-Object -First 1
if (!$jdk25 -or !$jdk21) { throw 'Java 21 and Java 25 are required in .tools/java and .tools/java25.' }
$env:JAVA_HOME = $jdk25.FullName
$env:JAVA_HOME_21 = $jdk21.FullName
$env:JAVA_HOME_25 = $jdk25.FullName
$env:JAVA_HOME_8 = (Get-ChildItem "$PSScriptRoot/.tools/java8" -Directory | Select-Object -First 1).FullName
$env:GRADLE_USER_HOME = "$PSScriptRoot/.tools/gradle"
$task = if ($Client) { 'runClient' } elseif ($Server) { 'runServer' } else { 'build' }
& ./gradlew.bat $task --no-daemon "-Porg.gradle.java.installations.paths=$($jdk21.FullName.Replace('\','/')),$($jdk25.FullName.Replace('\','/'))"
exit $LASTEXITCODE
