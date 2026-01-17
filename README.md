![Bedwars2023_Logo](https://github.com/tomkeuper/BedWars2023/assets/29728836/5979c0e8-1333-40a5-b43c-49ceabd06a57)

[![Discord](https://discordapp.com/api/guilds/1313655710508388393/widget.png?style=shield)](https://discord.gg/kPaBGwhmjf)

Este é um fork do plugin [BedWars2023](https://github.com/tomkeuper/BedWars2023/) inicialmente desenvolvido pelo [TomKeuper](https://github.com/tomkeuper/)

# Dúvidas
Caso tenhas dúvidas, chame o Tadeu neste discord: [Brazilian Spigot](https://discord.gg/XdRw3gB2gW)

# Descrição
BedWars é um minijogo onde você tem que defender sua cama e destruir os outros.  
Depois que sua cama for destruída, você não poderá reaparecer.

# Requisitos do sistema
Este software roda em [Spigot](https://www.spigotmc.org/) e NMS.
Os forks Spigot sem código NMS compilado não são suportados.
Servidores com suporte oficial são [Spigot](https://www.spigotmc.org/) e [Paper](https://papermc.io/).
Você precisa usar o Java 17 ou mais recente para executar este plugin.
Caso esteja na 1.8.8, use o [PandaSpigot](https://github.com/hpfxd/PandaSpigot).

Recomendamos no mínimo 8gb de RAM e um processador de qualidade boa.

O uso do [SlimeWorldManager](https://www.spigotmc.org/resources/slimeworldmanager.69974/) é obrigatório para gerenciamento dos mapas.

O uso do [TAB](https://www.spigotmc.org/resources/tab-1-7-x-1-21-11.57806/) é obrigatório para gerenciamento da Scoreboard e Tablist.

# Dependências
- Java 11
- Qualquer fork do Spigot com NMS
- Plugin [TAB](https://github.com/NEZNAMY/TAB)
- Plugin [SlimeWorldManager](https://www.spigotmc.org/resources/slimeworldmanager.69974/)

# Como instalar
- [Vídeo tutorial](https://youtu.be/WXTs2n4oKFs)
- [Discord para dúvidas](https://discord.gg/XdRw3gB2gW)

# Servidores prontos e addons da comunidade

Você pode achar na wiki oficial do TomKeuper, [clique aqui](https://wiki.tomkeuper.com/docs/BedWars2023/addons).
Tem muitos mais addons no discord oficial dele, [clique aqui](https://wiki.tomkeuper.com/docs/BedWars2023/addons).

Claro! Aqui está a **tradução completa para português**, com a **adição destacando que o modo MULTIARENA possui suporte a arenas infinitas com autoscaling**:

# Principais recursos

## Flexível | Formas de executar o plugin:

* **SHARED**: pode rodar junto com outros mini-games na mesma instância Spigot. Os jogos serão acessíveis apenas via comandos.
* **MULTIARENA**: requer uma instância inteira do servidor para hospedar o mini-game. Protege o mundo do lobby e os jogos podem ser acessados por comandos, NPCs, placas e GUIs.
  👉 **Este modo possui suporte a arenas infinitas com autoscaling automático**, criando e gerenciando arenas dinamicamente conforme a demanda de jogadores.
* **BUNGEE-LEGACY**: o modo bungee clássico antigo, onde cada jogo ocupa uma instância inteira do servidor. O jogador entra diretamente no jogo ao se conectar. O status da arena é exibido no MOTD.
* **BUNGEE**: um novo modo bungee escalável. Pode hospedar múltiplas arenas na mesma instância, clonar e iniciar novas arenas conforme necessário para permitir a entrada de mais jogadores. O servidor pode ser reiniciado automaticamente após uma certa quantidade de partidas. Requer a instalação do [BedWarsProxy](https://www.spigotmc.org/resources/bedwarsproxy.66642/) nos servidores de lobby. E, claro, você pode rodar quantos servidores quiser no modo bungee.

## Idioma | Sistema de idioma por jogador:

* Cada jogador pode receber mensagens, hologramas, GUIs etc. no idioma desejado. `/bw lang`
* Você pode remover ou adicionar novos idiomas.
* Nomes de times, grupos, conteúdos da loja e muito mais podem ser traduzidos.
* Títulos e subtítulos personalizados para a [contagem regressiva inicial](https://wiki.tomkeuper.com/docs/BedWars2023/configuration/language-configuration#custom-title-sub-title-for-arena-countdown).

## Remoção do lobby | Opcional:

O lobby de espera dentro do mapa pode ser removido assim que a partida iniciar.

## Grupos de Arenas | Customização:

* Você pode agrupar arenas por tipo (4v4, 50v50).
* Os grupos podem ter scoreboards personalizados, upgrades de time, itens iniciais e configurações de geradores próprias.
* É possível entrar em mapas por grupo: `/bw join Solo`, `/bw gui Solo`.

## Loja | Customização:

* Configuração de itens padrão do quick-buy.
* Adição ou remoção de categorias.
* Criação de novos itens ou execução de comandos ao comprar.
* Itens permanentes são recebidos após o respawn.
* Itens permanentes podem ser degradáveis, perdendo um nível por morte.
* Itens podem ter peso, impedindo a compra de itens mais fracos.
* Itens especiais: BedBug, Dream Defender, Egg Bridge, TNT Jump e Straight Fireball.
* O quick-buy é sincronizado entre nós no modo bungee.

## Upgrades de Time | Customização:

* Upgrades diferentes por grupo de arena.
* Adição e remoção de categorias e conteúdos.
* Upgrades podem encantar itens, aplicar efeitos de poção (em aliados, base ou inimigos), alterar geradores e definir a quantidade de dragões no Sudden Death.
* Armadilhas personalizadas que removem encantamentos, aplicam efeitos, removem efeitos de inimigos e executam comandos.

## Formas de entrar em uma arena:

* Seletor de arenas configurável. `/bw gui` mostra todos os grupos, `/bw gui Solo` apenas arenas Solo.
* Entrada via NPCs usando Citizens.
* Placas de entrada com bloco de status.
* Comandos:
  `/bw join random` → arena mais cheia
  `/bw join nomeMapa` → arena específica
  `/bw join grupo1+grupo2` → arena de grupos específicos

## Configurações de Arena | Customização:

* Nome de exibição personalizado para placas e GUIs.
* Definição de jogadores mínimos/máximos e tamanho do time.
* Opções de toggle: espectadores, geradores para times vazios, NPCs para times vazios, drops internos, hologramas da cama.
* Alcance de proteção para spawn e NPCs do time.
* Raio da ilha (armadilhas e borda do mapa).
* Morte instantânea no void baseada no eixo Y.
* Criação de quantos times quiser.
* Permitir quebra do mapa como no SkyWars.
* Ativar/desativar divisão de geradores.
* Regras de jogo personalizadas por mapa.
* Geradores ilimitados de ferro, ouro e esmeralda por time (ativável via upgrades).

## Kick VIP | Privilégio:

Jogadores com a permissão `bw.vip` podem entrar em arenas cheias na fase inicial, removendo um jogador sem essa permissão.

## Estatísticas do Jogador:

* O plugin não fornece hologramas de ranking, mas suporta ajLeaderboards e LeaderHeads via placeholders.
* Os jogadores podem ver suas estatísticas em uma GUI interna personalizada com `/bw stats`.

## Sistema de Party:

* Sistema interno simples e funcional para jogar com amigos.
* Suporte a Parties (AlessioDP) e Party and Friends (Simonsator), ideal para grandes networks.

## Sistema Anti-AFK:

Jogadores inativos por mais de 45 segundos não podem pegar itens dos geradores.

## Itens de Entrada Personalizados:

* Configuração de itens recebidos ao entrar no servidor (apenas multi-arena) ou ao entrar em uma partida ou como espectador.
* Itens podem executar comandos.

## Sistema de Restauração de Mapas:

* O método padrão restaura o mapa completo (descarrega, descompacta backup e carrega novamente).
* Recomendado uso de SSD e processadores voltados para jogos.
* Suporte ao **SlimeWorldManager**, com carregamento muito mais rápido e menor impacto de performance.
* Suporte a adaptadores personalizados via API.
* A restauração completa é necessária para permitir destruição total do mapa (estilo SkyWars).

## Reentrada | Re-Join:

Se desconectar ou sair da partida (configurável), você pode retornar via comando ou reconectando. Disponível também no modo bungee escalável.

## TNT Jump | Recurso:

* TNT Jump configurável.
* Jogadores com TNT possuem partículas vermelhas na cabeça (configurável).

## Eventos Sazonais:

* Evento especial de Halloween ativado automaticamente conforme o fuso horário da máquina, com efeitos especiais.

