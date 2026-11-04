
package com.managertm;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

public class RepoQuerys {
    public Request QuerysEnvios(String type, JSONArray conditions,JSONObject parameters,boolean cond) throws JsonProcessingException{
        String query;
        boolean actionType = false;
        switch (type) {
            case "TMCaba":
                query = "SELECT e.ID, e.ER, FechaEnvio, e.Pedido, Localidad, Cp, Direccion, DetalleDirec, p.Producto, p.Cant, d.Comuna, d.Zona, d.Cercania, d.Transporte " +
                        "FROM Envios AS e LEFT JOIN Pedidos AS p ON (e.Pedido = p.Pedido) LEFT JOIN (SELECT ER, MIN(Comuna) AS Comuna, MIN(Zona) AS Zona,"+
                        "MIN(Cercania) AS Cercania, MIN(Transporte) AS Transporte FROM ErDataAdic GROUP BY ER) AS d ON d.ER = e.ER where ";
                break;
            case "TMCENTRAL": case "MOVIL1":case "MOVIL2": case "CANCELADOS":case "sn":case "Remitos":
                query = "select e.ID,e.ER,FechaEnvio,e.Pedido, p.Producto,p.Cant,p.Registro,Transporte,Valid from Envios as e left join "+
                        "Pedidos as p on(e.Pedido = p.Pedido) left join ErDataAdic as d on(d.ER = e.ER) where ";
                break;
            case "RuteoTM":
                query = "select e.ER,FechaEnvio,Localidad,p.Producto,Direccion,DetalleDirec,p.Cant,Obs,Etiqueta " +
                        "from Envios as e left join Pedidos as p on(e.Pedido = p.Pedido) left join ErDataAdic as d on(d.ER = e.ER) where ";
                break;
            case "Etiquetas":
                query = "Select distinct(coalesce(Orden,0)) as Orden,e.ER,FechaEnvio,p.Pedido,Localidad,Cp,EstadoF,Cliente,p.Producto,Telefono,Direccion,DetalleDirec,Cant,Obs,Etiqueta,p.Familia,p.Registro," +
                        "Mail,NumeroGuia,Valid,ERId,pr.Ean,pr.CodigoBarra,pr.Alto,pr.Largo,pr.Profundidad,pr.Bultos,pr.Precio,p.PPid,Transporte from Envios as e join Pedidos as p on(e.Pedido = p.Pedido)" +
                        "left join ErDataAdic as d on(e.ER = d.ER) left join Productos pr on(p.Producto = pr.Denominacion) left join obleas o on(e.ER=o.ER and e.FechaEnvio=o.FechaCarga) where ";
                break;
            case "EtiquetasImpresas":
                query = "Select distinct(coalesce(Orden,0)) as Orden,e.ER,FechaEnvio,p.Pedido,Localidad,Cp,EstadoF,Cliente,p.Producto,Telefono,Direccion,DetalleDirec,Cant,Obs,Etiqueta,p.Familia,p.Registro," +
                        "Mail,NumeroGuia,Valid,ERId,pr.Ean,pr.CodigoBarra,pr.Alto,pr.Largo,pr.Profundidad,o.Bultos,pr.Precio,p.PPid,Transporte,o.Paquete,o.Oblea,FechaCarga from Envios as e join Pedidos as p on(e.Pedido = p.Pedido)" +
                        "left join ErDataAdic as d on(e.ER = d.ER) left join Productos pr on(p.Producto = pr.Denominacion) left join obleas o on(e.ER=o.ER) where ";
                break;
            case "UpdateLocalidadesAndreani":
                query = "Select distinct(coalesce(Orden,0)) as Orden,e.ER,FechaEnvio,p.Pedido,Localidad,Cp,EstadoF,Cliente,p.Producto,Telefono,Direccion,DetalleDirec,Cant,Obs,Etiqueta,p.Familia,p.Registro," +
                        "Mail,NumeroGuia,Valid,ERId,pr.Ean,pr.CodigoBarra,pr.Alto,pr.Largo,pr.Profundidad,pr.Bultos,pr.Precio,p.PPid from Envios as e join Pedidos as p on(e.Pedido = p.Pedido)" +
                        "left join ErDataAdic as d on(e.ER = d.ER) left join Productos pr on(p.Producto = pr.Denominacion) left join obleas o on(e.ER=o.ER) where ";
                actionType = true;
                break;
            case "EtiquetasRetRes":
                query = "select e.ER,Estado,EstadoF,Oblea,FechaEnvio,EstadoDevolucion,FormaEnvio,ERId from Envios e left join Obleas o on(e.ER = o.ER) where ";
                break;
            case "ReImpresion":
                query = "Select o.Oblea,e.ER,o.FechaCarga,p.Pedido,Localidad,Cp,EstadoF,Cliente,p.Producto,Telefono,Direccion,DetalleDirec," +
                        "Cant,Obs,Etiqueta,Familia,Registro,Mail,NumeroGuia,Valid,ERId,o.Orden,o.Paquete,o.Bultos,PPid,Estado,FormaEnvio "+
                        "from Envios as e join Pedidos as p on(e.Pedido = p.Pedido) left join Obleas as o on(e.ER = o.ER) where ";
                break;            
            case "Obleas":
                query = "select o.ER,Oblea,Estado,o.Producto,Direccion,Registro,ERId from Obleas as o join Envios as r on(o.ER=r.ER) join pedidos as p on(p.Pedido=r.Pedido) where ";
                break;
            case "Productos":
                query = "select * from Productos where Ean=@Ean or CodigoBarra=@CodigoBarra";
                break;
            case "ReporteDespachos":
                query = "select distinct(O.Oblea),R.ER,O.Producto,Paquete,Bultos,R.Cliente,R.Localidad,R.Cp,O.FechaControl " +
"                        from Obleas as O join Envios as R on (O.ER = R.ER) where ";
                break;
            case "PedidosSucursales":
                query = "select p.Ean,p.CodigoBarra,pp.Denominacion,Cant,Pedido,CantControl,Validacion,Scan,Suc,Fecha from Pedidosproductos pp " +
                        "left Join Productos p on(p.Denominacion=pp.Denominacion) where "; 
                break;
            case "ReporteGestionDevoluciones":
                query = "Select e.Id,e.ER,Oblea,e.FechaEnvio,p.Pedido,Localidad,coalesce(Estado,'') as Estado,EstadoF,Cliente,p.Producto,p.Cant,Direccion,Cp,DetalleDirec,p.Familia,Obs,Telefono,ERId " +
                        "from Envios as e join Pedidos as p on(e.Pedido = p.Pedido) left join Obleas o on(o.ER=e.ER) where ";
                break;
            case "Estados":
                query = "select o.ER,Oblea,Estado,EstadoF,o.Producto,Direccion,Registro,ERId from Obleas as o join Envios as r on(o.ER=r.ER) join pedidos as p on(p.Pedido=r.Pedido) where Estado not in " +
                        "('Cliente Retiro','REVISADO','Recibido Cliente', 'Devuelto', 'Envio Cancelado', 'Listo para Enviar','Anulado', 'Devolucion Pendiente','Devolucion Recibida','Siniestrado') ";
                break;
            default:
                query = "";
                break;
        }
        Request request = new Request();
        request.Controller = actionType == true ? "UPDATE" : "GET";
        request.Tabla = "Logistica";
        request.Accion = actionType == true ? type : "GetQueryConstructor";
        if(conditions != null && cond){
            query += finalConditions(conditions);
        }
        request.Parametro1 = query;
        request.Parametro2 = parameters != null ? parameters.toString() : "";
        return request;
    }
    private String finalConditions(JSONArray fields){
        StringBuilder whereClause = new StringBuilder();
        for(int i=0;i<fields.length();i++){
            String rawField= fields.getString(i);
            boolean useLike = rawField.startsWith("-");
            boolean useEqual = rawField.startsWith("*");
            String field = useLike ? rawField.substring(1): rawField;
            field = useEqual ? rawField.substring(1): field;
            if(i>0) whereClause.append(" AND ");
            
            if(useLike){
                whereClause.append(field).append(" LIKE @").append(field);
            }else if(useEqual){
                whereClause.append(field).append("<>@").append(field);
            }else{
                whereClause.append(field).append("=@").append(field);
            }            
        }
        return whereClause.toString();
    }
            
            
}
