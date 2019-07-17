
# Define default protocol http/https


*   com.compxc.workbench.presentation.Application.configureApplication() => aProtocol = "http"/"https"
*   com.profidatagroup.javamis.env.param.ParamNetwork.transformToConnection(String) => ret.setPrototcol("http"/"https")
*   default\_instance\_global.cfg => export EAMIS\_HTTPGATEWAY\_SECURE=false/true
*   XCAS.lua => set BO\_HTTP\_PROTOCOL based on the value of EAMIS\_HTTPGATEWAY\_SECURE
*   XCAS configuration.properties => default value vor BO\_HTTP\_PROTOCOL=http/https
*   com.profidata.healthmonitor.app.HealthMonitorMain.main() => aProtocol = "http"/"https"
