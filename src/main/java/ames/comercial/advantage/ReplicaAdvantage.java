package ames.comercial.advantage;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ReplicaAdvantage {

    private static final ThreadLocal<Integer> contadorServicios = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<ReplicaAdvantageData> replicaAdvantageData = ThreadLocal.withInitial(ReplicaAdvantageData::new);

    // Punto de corte para ejecutar el aspecto en métodos dentro de clases anotadas con @Service
    @Pointcut("@within(org.springframework.stereotype.Service)")
    public void dentroDeServicio() {}

    // Método que se ejecuta antes de la ejecución de cada servicio
    @Before("dentroDeServicio()")
    public void registrarServicio() {
        contadorServicios.set(contadorServicios.get() + 1);
    }

    // Método que se ejecuta después de la ejecución de cualquier servicio
    @AfterReturning("dentroDeServicio()")
    public void ejecutarDespuesDeMetodo() {
        if (contadorServicios.get() == 1) {
            new ReplicaAdvantageExecutador().executar();
        }
        // Decrementar el contador
        contadorServicios.set(contadorServicios.get() - 1);
    }

    // Advice para manejar excepciones lanzadas por los métodos que coinciden con el pointcut
    @AfterThrowing(pointcut = "dentroDeServicio()", throwing = "exception")
    public void ejecutarExcepcion(JoinPoint joinPoint, Exception exception) {
        cleanThreadLocal();
    }

    public static ReplicaAdvantageData instance() {
        return replicaAdvantageData.get();
    }

    public static void cleanThreadLocal() {
        contadorServicios.remove();
        replicaAdvantageData.remove();
    }

}
