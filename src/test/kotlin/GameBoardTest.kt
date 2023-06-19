import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

val characterTest = Character("CharacterTest", 5, "TestAbility", "TestDifficulty")

val dominatedPlayer = Player(
    "DominatedPlayer",
    Role.DOMINATED,
    mutableMapOf(
        CapitalType.FINANCIAL to 0,
        CapitalType.CULTURAL to 0,
        CapitalType.SOCIAL to 0,
        CapitalType.SYMBOLIC to 0,
    ),
    characterTest,
    0,
    false,
)

val dominatedPlayer2 = Player(
    "DominatedPlayer2",
    Role.DOMINATED,
    mutableMapOf(
        CapitalType.FINANCIAL to 0,
        CapitalType.CULTURAL to 0,
        CapitalType.SOCIAL to 0,
        CapitalType.SYMBOLIC to 0,
    ),
    characterTest,
    0,
    false,
)

val dominatedPlayer3 = Player(
    "DominatedPlayer3",
    Role.DOMINATED,
    mutableMapOf(
        CapitalType.FINANCIAL to 0,
        CapitalType.CULTURAL to 0,
        CapitalType.SOCIAL to 0,
        CapitalType.SYMBOLIC to 0,
    ),
    characterTest,
    0,
    false,
)


val dominantPlayer = Player(
    "DominantPlayer",
    Role.DOMINANT,
    mutableMapOf(
        CapitalType.FINANCIAL to 0,
        CapitalType.CULTURAL to 0,
        CapitalType.SOCIAL to 0,
        CapitalType.SYMBOLIC to 0,
    ),
    characterTest,
    0,
    false,
)

// Carta 1: Adiciona recursos financeiros
val card1 = Card(
    description = "Você recebeu um bônus! Adicione 5 unidades de capital financeiro.",
    action = { player -> player.addCapital(CapitalType.FINANCIAL, 5) },
    role = Role.DOMINATED // ou Role.DOMINANT, dependendo do seu jogo
)

// Carta 2: Remove recursos financeiros
val card2 = Card(
    description = "Você pagou uma multa! Remova 3 unidades de capital financeiro.",
    action = { player -> player.subtractCapital(CapitalType.FINANCIAL, 3) },
    role = Role.DOMINATED // ou Role.DOMINANT, dependendo do seu jogo
)

// Carta 3: Adiciona recursos culturais
val card3 = Card(
    description = "Você aprendeu uma nova habilidade! Adicione 2 unidades de capital cultural.",
    action = { player -> player.addCapital(CapitalType.CULTURAL, 2) },
    role = Role.DOMINATED // ou Role.DOMINANT, dependendo do seu jogo
)

// Carta 4: Remove recursos sociais
val card4 = Card(
    description = "Você perdeu alguns amigos! Remova 1 unidade de capital social.",
    action = { player -> player.subtractCapital(CapitalType.SOCIAL, 1) },
    role = Role.DOMINATED // ou Role.DOMINANT, dependendo do seu jogo
)

// Carta 5: Adiciona recursos simbólicos
val card5 = Card(
    description = "Você ganhou uma medalha! Adicione 4 unidades de capital simbólico.",
    action = { player -> player.addCapital(CapitalType.SYMBOLIC, 4) },
    role = Role.DOMINATED // ou Role.DOMINANT, dependendo do seu jogo
)

// Carta 1: Adiciona recursos financeiros
val dominantCard1 = Card(
    description = "Seu investimento rendeu frutos! Adicione 20 unidades de capital financeiro.",
    action = { player -> player.addCapital(CapitalType.FINANCIAL, 20) },
    role = Role.DOMINANT
)

// Carta 2: Remove recursos financeiros
val dominantCard2 = Card(
    description = "Você teve um grande prejuízo! Remova 15 unidades de capital financeiro.",
    action = { player -> player.subtractCapital(CapitalType.FINANCIAL, 15) },
    role = Role.DOMINANT
)

// Carta 3: Adiciona recursos culturais
val dominantCard3 = Card(
    description = "Você fez uma grande descoberta! Adicione 10 unidades de capital cultural.",
    action = { player -> player.addCapital(CapitalType.CULTURAL, 10) },
    role = Role.DOMINANT
)

// Carta 4: Remove recursos sociais
val dominantCard4 = Card(
    description = "Você foi pego em um escândalo! Remova 8 unidades de capital social.",
    action = { player -> player.subtractCapital(CapitalType.SOCIAL, 8) },
    role = Role.DOMINANT
)

// Carta 5: Adiciona recursos simbólicos
val dominantCard5 = Card(
    description = "Você foi homenageado pela sua contribuição! Adicione 15 unidades de capital simbólico.",
    action = { player -> player.addCapital(CapitalType.SYMBOLIC, 15) },
    role = Role.DOMINANT
)


class GameBoardTest {
    lateinit var gameBoard: GameBoard

    @BeforeEach
    fun setup() {
        gameBoard = GameBoard(
            cardsDeck = mutableListOf(
                card1,
                card2,
                card3,
                card4,
                card5,
                dominantCard1,
                dominantCard2,
                dominantCard3,
                dominantCard4,
                dominantCard5
            ),
            dominantPlayer = dominantPlayer,
            dominatedPlayers = mutableListOf(dominatedPlayer, dominatedPlayer2, dominatedPlayer3)
        )
    }

    @Test
    fun `test shuffleCards`() {
        val deckBeforeShuffle = gameBoard.cardsDeck.toList()
        gameBoard.shuffleCards()
        val deckAfterShuffle = gameBoard.cardsDeck
        assert(deckBeforeShuffle != deckAfterShuffle)
    }

    @Test
    fun `test startGame`() {
        gameBoard.startGame()
        assertEquals(10, dominantPlayer.capital[CapitalType.FINANCIAL])
        assertEquals(2, dominatedPlayer.capital[CapitalType.FINANCIAL])
        assertEquals(10, dominantPlayer.capital[CapitalType.CULTURAL])
        assertEquals(2, dominatedPlayer.capital[CapitalType.CULTURAL])
        assertEquals(10, dominantPlayer.capital[CapitalType.SOCIAL])
        assertEquals(2, dominatedPlayer.capital[CapitalType.SOCIAL])
        assertEquals(10, dominantPlayer.capital[CapitalType.SYMBOLIC])
        assertEquals(2, dominatedPlayer.capital[CapitalType.SYMBOLIC])
    }

    @Test
    fun `test goToPrison for dominated player`() {
        gameBoard.goToPrison(dominatedPlayer)
        assert(dominatedPlayer.skipTurn)
    }

    @Test
    fun `test goToPrison for dominant player`() {
        dominantPlayer.capital[CapitalType.FINANCIAL] = 15
        gameBoard.goToPrison(dominantPlayer)
        assertEquals(5, dominantPlayer.capital[CapitalType.FINANCIAL])
    }

    @Test
    fun `test makeGeneralStrike`() {
        dominantPlayer.capital[CapitalType.FINANCIAL] = 10
        dominantPlayer.capital[CapitalType.CULTURAL] = 10
        gameBoard.makeGeneralStrike()
        assert(dominantPlayer.skipTurn)
        assertEquals(4, dominantPlayer.capital[CapitalType.FINANCIAL])
        assertEquals(9, dominantPlayer.capital[CapitalType.CULTURAL])
    }

    @Test
    fun `test addCapital and subtractCapital`() {
        dominatedPlayer.addCapital(CapitalType.FINANCIAL, 5)
        assertEquals(5, dominatedPlayer.capital[CapitalType.FINANCIAL])

        dominatedPlayer.subtractCapital(CapitalType.FINANCIAL, 3)
        assertEquals(2, dominatedPlayer.capital[CapitalType.FINANCIAL])
    }

    @Test
    fun `test draftCard`() {
        val card = gameBoard.draftCard(Role.DOMINATED)
        assertEquals(9, gameBoard.cardsDeck.size)
        assert(card.role == Role.DOMINATED)
    }

    @Test
    fun `test makeRevolution`() {
        dominatedPlayer.addCapital(CapitalType.FINANCIAL, 15)
        dominatedPlayer2.addCapital(CapitalType.FINANCIAL, 15)
        dominatedPlayer3.addCapital(CapitalType.FINANCIAL, 15)

        dominantPlayer.capital[CapitalType.FINANCIAL] = 30
        dominantPlayer.capital[CapitalType.CULTURAL] = 30
        dominantPlayer.capital[CapitalType.SOCIAL] = 30
        dominantPlayer.capital[CapitalType.SYMBOLIC] = 30

        gameBoard.makeRevolution()

        assertEquals(25, dominantPlayer.capital[CapitalType.CULTURAL])
        assertEquals(25, dominantPlayer.capital[CapitalType.SOCIAL])
        assertEquals(25, dominantPlayer.capital[CapitalType.SYMBOLIC])
        assertEquals(25, dominantPlayer.capital[CapitalType.FINANCIAL])
    }
}