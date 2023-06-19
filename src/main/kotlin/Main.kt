import kotlin.math.max
import kotlin.math.min

// Enum para definir o papel do jogador
enum class Role {
    DOMINANT, DOMINATED
}

// Enum para tipos de capital
enum class CapitalType {
    FINANCIAL, SOCIAL, CULTURAL, SYMBOLIC
}

// Enum para representar as casas especiais
enum class Square {
    NORMAL, REVOLUTION, GENERAL_STRIKE, ALL_TOGETHER, PRISON
}

// Classe para representar um personagem
data class Character(
    var name: String,
    var endGameBonus: Int,
    var ability: String,
    var difficulty: String,
)

// Classe para representar um jogador
data class Player(
    var name: String,
    var role: Role,
    val capital: MutableMap<CapitalType, Int>,
    var character: Character,
    var position: Int, // posição do jogador no tabuleiro
    var skipTurn: Boolean = false,

    ) {
    fun addCapital(capitalType: CapitalType, value: Int) {
        this.capital[capitalType] = (this.capital[capitalType] ?: 0) + value
    }

    fun subtractCapital(capitalType: CapitalType, value: Int) {
        this.capital[capitalType] = max(0, (this.capital[capitalType] ?: 0) - value)
    }
}

// Classe para representar uma carta
data class Card(
    val description: String,
    val action: (Player) -> Unit,
    val role: Role,
)

// Classe para o tabuleiro do jogo
class GameBoard(
    var cardsDeck: MutableList<Card> = mutableListOf(),
    var dominantPlayer: Player,
    var dominatedPlayers: MutableList<Player> = mutableListOf(),
) {

    val players: List<Player>
        get() {
            return listOf(dominantPlayer) + dominatedPlayers
        }
    var turn = 0

    val squares: Array<Square> = Array(76) { value ->
        when (value) {
            11, 59 -> Square.GENERAL_STRIKE
            19, 68 -> Square.REVOLUTION
            28, 48 -> Square.PRISON
            37, 71 -> Square.ALL_TOGETHER
            else -> Square.NORMAL
        }
    }

    // Método para embaralhar as cartas
    fun shuffleCards() {
        cardsDeck.shuffle()
    }

    // Método para iniciar o jogo
    fun startGame() {
        shuffleCards()
        players.forEach { player ->
            val initialCapital = if (player.role == Role.DOMINANT) {
                10
            } else {
                2
            }

            player.capital[CapitalType.FINANCIAL] =  initialCapital
            player.capital[CapitalType.CULTURAL] =  initialCapital
            player.capital[CapitalType.SOCIAL] =  initialCapital
            player.capital[CapitalType.SYMBOLIC] =  initialCapital
        }
    }

    fun playTurn() {
        val playerOfTurn = players[turn % players.size]

        // Verificar se o jogador está com penalidade de turno
        if (playerOfTurn.skipTurn) {
            playerOfTurn.skipTurn = false
            // Passar a vez para o próximo jogador
            turn++
            return
        }

        playerOfTurn.position += playDice()
        when (squares[playerOfTurn.position]) {
            Square.NORMAL -> {
                val card = draftCard(playerOfTurn.role)
                card.action(playerOfTurn)
            }

            Square.REVOLUTION -> makeRevolution()
            Square.GENERAL_STRIKE -> makeGeneralStrike()
            // TODO Implement player pick
            Square.ALL_TOGETHER -> makeAllTogether(
                dominatedPlayers.map {
                    CapitalType.values().random()
                },
            )

            Square.PRISON -> goToPrison(playerOfTurn)
        }

        turn++
    }

    fun goToPrison(player: Player) {
        if (player.role == Role.DOMINATED) {
            player.skipTurn = true
        } else {
            player.subtractCapital(CapitalType.FINANCIAL, 10)
        }
    }

    fun makeAllTogether(chosenCapitals: List<CapitalType>) {
        // Verifica se cada jogador dominado fez uma escolha
        if (chosenCapitals.size != dominatedPlayers.size) {
            throw IllegalArgumentException("Cada jogador dominado deve escolher um tipo de capital")
        }

        // Se houver um jogador dominante, cada jogador dominado pode exigir 2 unidades de capital dele
        dominantPlayer.let { dominant ->
            for (i in dominatedPlayers.indices) {
                val dominated = dominatedPlayers[i]
                val chosenCapital = chosenCapitals[i]

                val transferAmount = min(dominantPlayer.capital[chosenCapital]!!, 2)
                dominated.addCapital(chosenCapital, transferAmount)
                dominantPlayer.subtractCapital(chosenCapital, transferAmount)
            }
        }
    }

    fun makeGeneralStrike() {
        // se há um jogador dominante, ele perde uma rodada e perde kapitais
        dominantPlayer.let { dominant ->
            dominant.skipTurn = true
            dominant.subtractCapital(CapitalType.FINANCIAL, 6)
            for (capitalType in CapitalType.values().filter { it != CapitalType.FINANCIAL }) {
                dominant.subtractCapital(capitalType, 1)
            }
        }
    }

    fun makeRevolution() {
        // reunir todos os capitais de todos os jogadores
        val totalCapitals = mutableMapOf<CapitalType, Int>()
        for (capitalType in CapitalType.values()) {
            totalCapitals[capitalType] = 0
        }
        for (player in players) {
            for (capitalType in CapitalType.values()) {
                player.capital[capitalType] = totalCapitals[capitalType]!! / players.size
            }
        }

        // distribuir igualmente entre os jogadores
        for (player in players) {
            for (capitalType in CapitalType.values()) {
                player.capital[capitalType] = totalCapitals[capitalType]!! / players.size
            }
        }

        // o jogador dominante recebe o excedente
        for ((capitalType, total) in totalCapitals) {
            val leftover = total % players.size
            if (leftover > 0) {
                dominantPlayer.capital[capitalType] = dominantPlayer.capital[capitalType]!! + leftover
            }
        }
    }

    fun draftCard(role: Role): Card {
        // Filtrar as cartas pelo papel do jogador
        val roleCards = cardsDeck.filter { it.role == role }

        // Verificar se há cartas para o papel do jogador
        if (roleCards.isEmpty()) {
            throw RuntimeException("Não há mais cartas para o papel: $role")
        }

        // Selecionar uma carta aleatoriamente
        val card = roleCards.random()

        // Remover a carta selecionada do baralho
        cardsDeck.remove(card)

        // Retornar a carta selecionada
        return card
    }

    fun playDice(): Int {
        return (1..6).random()
    }
}
