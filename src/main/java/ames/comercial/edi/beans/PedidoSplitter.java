package ames.comercial.edi.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PedidoSplitter {

    public static Map<String, ComandaMissatgeEDI> dividirPedidoPorCodigoConsignatario(ComandaMissatgeEDI pedido) {
            Map<String, List<Linea>> agrupadosPorCodigo = new HashMap<>();

            // Agrupar las líneas por el valor de `codigoConsignatario`
            for (Linea linea : pedido.getLineas()) {
                String codigo = linea.getLC().getCodigoConsignatario();

                // Si no existe una lista para este código, se crea
                agrupadosPorCodigo.putIfAbsent(codigo, new ArrayList<>());

                // Añadir la línea a la lista correspondiente
                agrupadosPorCodigo.get(codigo).add(linea);
            }

            // Convertir las listas agrupadas en objetos Pedido, manteniendo CA, CB, etc.
            Map<String, ComandaMissatgeEDI> pedidosAgrupados = new HashMap<>();
            for (Map.Entry<String, List<Linea>> entry : agrupadosPorCodigo.entrySet()) {
                String codigoConsignatario = entry.getKey();
                List<Linea> lineasAgrupadas = entry.getValue();

                // Crear un nuevo pedido con las líneas agrupadas y los atributos del pedido original
                ComandaMissatgeEDI nuevoPedido = new ComandaMissatgeEDI(pedido.getCA(), pedido.getCB(), pedido.getCC(),pedido.getCD(),pedido.getCI(),
                        pedido.getCP(),pedido.getCQ(),pedido.getCF(),pedido.getTextosLibres(),pedido.getZZ(),lineasAgrupadas);
                pedidosAgrupados.put(codigoConsignatario, nuevoPedido);
            }

            return pedidosAgrupados;
    }
}
