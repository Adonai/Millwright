# Millwright
Very specific request handling app that I was working on when freelancing

## Workflow

1. Operator sends specially-crafted SMS message to the worker's telephone
2. Application intercepts this SMS as it was sent from number in its' settings
3. Application then creates request entity suitable for processing (address, date etc.)
4. Worker clicks on request when it's completed and selects from options (completed, moved, denied)
5. When worker clicks "Ok", application deletes/moves request according to selected choice
6. Application sends crafted SMS with report and commentary from worker back to operator
