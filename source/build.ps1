function Exec([scriptblock]$cmd, [string]$errorMessage = "Error executing command: " + $cmd) {
  & $cmd
  if ($LastExitCode -ne 0) {
    throw ($errorMessage + "Exit code was " + $LastExitCode)
  }
}


cls

write-output "Building..."
exec { 
    ant 
}

write-output "Copy to TeamCity plugins directory..."
copy-item .\dist\Octopus.TeamCity.zip -Destination C:\TeamCity\.BuildServer\plugins

write-output "Restart TeamCity service..."
restart-service -name TeamCity

write-output "Restarted successful!"
