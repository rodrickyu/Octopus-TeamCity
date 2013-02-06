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
$service = 'TeamCity'
Stop-Service $service
do { Start-Sleep -Milliseconds 200}
until ((get-service $service).status -eq 'Stopped')

start-service $service

write-output "Restarted successful!"
