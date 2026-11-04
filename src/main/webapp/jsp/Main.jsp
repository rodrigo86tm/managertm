<div id="mapa"></div>
        
        <script>
             fetch('json/map.json')
                .then(response => response.json())
                .then(data => {
                    console.log(data); // Inspecciona qué propiedades existen

                    if (data.features) {
                        // Procesa las features del GeoJSON y crea una serie con las provincias
                        const provinces = data.features.map(feature => {
                            const provinceName = feature.properties.NAME_1; // Nombre de la provincia
                            return {
                                name: provinceName,
                                value: Math.random() * 1000  // Valor aleatorio para visualización, puedes usar otros datos aquí
                            };
                        });
                        // Registra el mapa de Argentina en ECharts
                        echarts.registerMap('argentina', data);

                        // Inicializa el gráfico
                        var chart = echarts.init(document.getElementById('mapa'));
                        chart.setOption({
                            backgroundColor: '#404a59',
                            title : {
                                text: 'Mapa de Argentina',
                                subtext: 'Datos ficticios',
                                left: 'center',
                                textStyle : {
                                    color: '#fff'
                                }
                            },
                            tooltip : {
                                trigger: 'item'
                            },
                            legend: {
                                orient: 'vertical',
                                top: 'bottom',
                                left: 'right',
                                data: provinces.map(p => p.name), // Agrega las provincias a la leyenda
                                textStyle: {
                                    color: '#fff'
                                }
                            },
                            geo: {
                                map: 'argentina',
                                label: {
                                    show: true,
                                    formatter:  function (params) {
                                        console.log(params);
                                        return params.name;  // Usar NAME_1 como nombre de la provincia
                                    }, // Muestra el nombre de la provincia
                                    color: 'black'
                                },
                                roam: true,
                                itemStyle: {
                                    normal: {
                                        areaColor: '#323c48',
                                        borderColor: '#404a59'
                                    },
                                    emphasis: {
                                        areaColor: '#2a333d'
                                    }
                                }
                            },
                            series: [{
                                name: 'Provincias',
                                type: 'map',
                                geoIndex: 0,
                                data: provinces
                            }]
                        });
                    } else {
                        console.error("No se encontraron features en el archivo GeoJSON");
                    }
                    chart.getDom().addEventListener('wheel', function(event) {
                        event.preventDefault();  // Usar preventDefault dentro de un evento pasivo
                    }, { passive: true });
                })
                .catch(error => {
                    console.error("Error al cargar el archivo GeoJSON:", error);
                });
        </script>
