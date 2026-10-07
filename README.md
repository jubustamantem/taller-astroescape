# AstroEscape

La empresa ficticia AstroEscape organiza viajes turísticos interplanetarios para personas que quieren visitar destinos como la Luna, Marte, Europa o Saturno. Ofrece viajes individuales y grupales, experiencias especiales —por ejemplo, caminatas lunares o fotografía de anillos de Saturno— y alquiler de trajes espaciales.

Este repositorio contiene un modelo de clases en Java de ese dominio. El código compila como un paquete (`astroescape`) y deja a la vista decisiones de diseño para analizar como ejercicio pedagógico.

## Clases

| Clase | Rol en el dominio |
| --- | --- |
| `Destino` | Lugar visitable, como la Luna o Saturno |
| `Experiencia` | Actividad ofrecida en un destino |
| `Viaje` | Viaje, con código, destino y fecha de salida |
| `ViajeIndividual` | Viaje para un solo cliente |
| `ViajeGrupal` | Viaje para varios clientes |
| `Cliente` | Persona que reserva o alquila |
| `TrajeEspacial` | Traje disponible para alquiler |
| `Alquiler` | Alquiler de un traje por un cliente |

## Qué deben hacer

El trabajo es en grupo. Cada grupo caracteriza **las dos clases que le asigne el docente**.

1. **Relaciones y multiplicidades.** Donde aparece el comentario `// Aquí hay una relación`, identifiquen el tipo de relación y las multiplicidades. Deben inferir esto a partir de los campos, las colecciones, las validaciones y la forma en que se crean los objetos.
2. **Principio de diseño que se rompe.** En una de las dos clases hay un método marcado con una pregunta. Respondan esa pregunta: qué principio de diseño no se está cuidando y cómo debería quedar el código.

No hace falta corregir el código. La entrega es la caracterización de sus dos clases, de manera escrita.
