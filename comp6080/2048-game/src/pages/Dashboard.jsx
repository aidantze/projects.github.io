import { useState, useEffect, useCallback } from 'react';
import { Button } from '@headlessui/react';

import Navbar from '../components/Navbar';

const INITIAL_BOARD = [
  [0, 0, 0, 0],
  [0, 0, 0, 0],
  [0, 0, 0, 0],
  [0, 0, 0, 0],
]

const Dashboard = ({ gamesCount, updateGamesCount }) => {
  const [board, setBoard] = useState(INITIAL_BOARD);
  const [isActive, setActive] = useState(false);
  const [isWon, setWon] = useState(false);
  const [isLost, setLost] = useState(false);

  useEffect(() => {
    document.title = "2048 Game";
  }, []);

  const arraysEqual = (a, b) =>
    a.length === b.length &&
    a.every((val, index) => val === b[index]);

  const canMoveLeft = useCallback(() => {
    for (let r = 0; r < board.length; r++) {
      if (board[r].includes(0)) {
        // all rows filled with leading 0s at the end
        let newRow = [...board[r]];
        newRow.sort((a, b) => (a === 0) - (b === 0));
        if (!arraysEqual(board[r], newRow)) return true;
        // any two adjacent squares have the same number
        if ((board[r][0] !== 0 && board[r][0] === board[r][1]) ||
          (board[r][1] !== 0 && board[r][1] === board[r][2]) ||
          (board[r][2] !== 0 && board[r][2] === board[r][3])) {
          return true;
        }
      } else if (board[r][0] === board[r][1] || board[r][1] === board[r][2] || board[r][2] === board[r][3]) {
        return true;
      }
    }
    return false;
  }, [board]);

  const canMoveRight = useCallback(() => {
    for (let r = 0; r < board.length; r++) {
      if (board[r].includes(0)) {
        // all rows filled with leading 0s at the start
        let newRow = [...board[r]];
        newRow.sort((a, b) => (a !== 0) - (b !== 0));
        if (!arraysEqual(board[r], newRow)) return true;
        if ((board[r][0] !== 0 && board[r][0] === board[r][1]) ||
          (board[r][1] !== 0 && board[r][1] === board[r][2]) ||
          (board[r][2] !== 0 && board[r][2] === board[r][3])) {
          return true;
        }
      } else if (board[r][0] === board[r][1] || board[r][1] === board[r][2] || board[r][2] === board[r][3]) {
        return true;
      }
    }
    return false;
  }, [board]);

  const canMoveUp = useCallback(() => {
    for (let c = 0; c < board.length; c++) {
      let col = [board[0][c], board[1][c], board[2][c], board[3][c]];
      if (col.includes(0)) {
        // all cols filled with leading 0s at the end
        let newCol = [...col];
        newCol.sort((a, b) => (a === 0) - (b === 0));
        if (!arraysEqual(col, newCol)) return true;
        if ((col[0] !== 0 && col[0] === col[1]) ||
          (col[1] !== 0 && col[1] === col[2]) ||
          (col[2] !== 0 && col[2] === col[3])) {
          return true;
        }
      } else if (col[0] === col[1] || col[1] === col[2] || col[2] === col[3]) {
        return true;
      }
    }
    return false;
  }, [board]);

  const canMoveDown = useCallback(() => {
    for (let c = 0; c < board.length; c++) {
      let col = [board[0][c], board[1][c], board[2][c], board[3][c]];
      if (col.includes(0)) {
        // all cols filled with leading 0s at the start
        let newCol = [...col];
        newCol.sort((a, b) => (a !== 0) - (b !== 0));
        if (!arraysEqual(col, newCol)) return true;
        if ((col[0] !== 0 && col[0] === col[1]) ||
          (col[1] !== 0 && col[1] === col[2]) ||
          (col[2] !== 0 && col[2] === col[3])) {
          return true;
        }
      } else if (col[0] === col[1] || col[1] === col[2] || col[2] === col[3]) {
        return true;
      }
    }
    return false;
  }, [board]);

  if (isActive && board.flat(Infinity).includes(2048)) {
    console.log("Game is won!");
    setWon(true);
    updateGamesCount();
  }

  if (isActive && !board.flat(Infinity).includes(0) && !canMoveLeft() && !canMoveRight() && !canMoveUp() && !canMoveDown()) {
    console.log("Game is lost!");
    setLost(true);
    setActive(false);
  }

  const hasZero = useCallback(() => {
    return board.flat(Infinity).includes(0);
  }, [board]);

  const randomiseZero = useCallback(() => {
    let zeroPositions = [];
    for (let r = 0; r < board.length; r++) {
      for (let c = 0; c < board[r].length; c++) {
        if (board[r][c] === 0) {
          zeroPositions.push({ r, c });
        }
      }
    }

    if (zeroPositions.length > 0) {
      let randIndex = Math.floor(Math.random() * zeroPositions.length);
      let { r, c } = zeroPositions[randIndex];

      let newBoard = [...board];
      newBoard[r][c] = 2;
      setBoard(newBoard);
    }
  }, [board]);

  const restartGame = () => {
    setBoard([
      [0, 0, 0, 0],
      [0, 0, 0, 0],
      [0, 0, 0, 0],
      [0, 0, 0, 0],
    ]);
    setActive(false);
    setWon(false);
    setLost(false);
  }

  const startGame = () => {
    restartGame();
    setActive(true);
    randomiseZero();
    randomiseZero();
    randomiseZero();
  }

  useEffect(() => {
    const handleLeft = () => {
      /* approach
      - if anything already at far left, skip it
      - if a 0 exists, then another number anywhere after it, replace 0 with next number after
      - only required to check up to 3 places away
      - if a number is moved, numbers after that also need to move. replace one 0 at a time by relocating to the end of the array
      */
      let newBoard = [...board];
      for (let r = 0; r < newBoard.length; r++) {
        let row = newBoard[r];
        row.sort((a, b) => (a === 0) - (b === 0));

        if (row[0] === row[1]) {
          row[0] = row[0] + row[1];
          row[1] = row[2];
          row[2] = row[3];
          row[3] = 0;
        }
        if (row[1] === row[2]) {
          row[1] = row[1] + row[2];
          row[2] = row[3];
          row[3] = 0;
        }
        if (row[2] === row[3]) {
          row[2] = row[2] + row[3];
          row[3] = 0;
        }

        newBoard[r] = row;
      }
      setBoard(newBoard);

      if (hasZero()) {
        randomiseZero();
      }
    }

    const handleRight = () => {
      let newBoard = [...board];
      for (let r = 0; r < newBoard.length; r++) {
        let row = newBoard[r];
        row.sort((a, b) => (a !== 0) - (b !== 0));

        if (row[3] === row[2]) {
          row[3] = row[3] + row[2];
          row[2] = row[1];
          row[1] = row[0];
          row[0] = 0;
        }
        if (row[2] === row[1]) {
          row[2] = row[2] + row[1];
          row[1] = row[0];
          row[0] = 0;
        }
        if (row[1] === row[0]) {
          row[1] = row[1] + row[0];
          row[0] = 0;
        }

        newBoard[r] = row;
      }
      setBoard(newBoard);

      if (hasZero()) {
        randomiseZero();
      }
    }

    const handleUp = () => {
      let newBoard = [...board];
      for (let c = 0; c < newBoard.length; c++) {
        let col = [newBoard[0][c], newBoard[1][c], newBoard[2][c], newBoard[3][c]];
        col.sort((a, b) => (a === 0) - (b === 0));

        if (col[0] === col[1]) {
          col[0] = col[0] + col[1];
          col[1] = col[2];
          col[2] = col[3];
          col[3] = 0;
        }
        if (col[1] === col[2]) {
          col[1] = col[1] + col[2];
          col[2] = col[3];
          col[3] = 0;
        }
        if (col[2] === col[3]) {
          col[2] = col[2] + col[3];
          col[3] = 0;
        }

        newBoard[0][c] = col[0];
        newBoard[1][c] = col[1];
        newBoard[2][c] = col[2];
        newBoard[3][c] = col[3];
      }
      setBoard(newBoard);

      if (hasZero()) {
        randomiseZero();
      }
    }

    const handleDown = () => {
      let newBoard = [...board];
      for (let c = 0; c < newBoard.length; c++) {
        let col = [newBoard[0][c], newBoard[1][c], newBoard[2][c], newBoard[3][c]];
        col.sort((a, b) => (a !== 0) - (b !== 0));

        if (col[3] === col[2]) {
          col[3] = col[3] + col[2];
          col[2] = col[1];
          col[1] = col[0];
          col[0] = 0;
        }
        if (col[2] === col[1]) {
          col[2] = col[2] + col[1];
          col[1] = col[0];
          col[0] = 0;
        }
        if (col[1] === col[0]) {
          col[1] = col[1] + col[0];
          col[0] = 0;
        }

        newBoard[0][c] = col[0];
        newBoard[1][c] = col[1];
        newBoard[2][c] = col[2];
        newBoard[3][c] = col[3];
      }
      setBoard(newBoard);

      if (hasZero()) {
        randomiseZero();
      }
    }

    const handleKeyDown = (e) => {
      // prevent slide change when typing in input element
      const activeTag = document.activeElement?.tagName;
      if (!isActive || isWon || isLost || activeTag === 'INPUT' || activeTag === 'TEXTAREA') {
        return;
      }

      switch (e.key) {
        case 'ArrowLeft':
          e.preventDefault();
          if (canMoveLeft()) handleLeft();
          break;
        case 'ArrowUp':
          e.preventDefault();
          if (canMoveUp()) handleUp();
          break;
        case 'ArrowRight':
          e.preventDefault();
          if (canMoveRight()) handleRight();
          break;
        case 'ArrowDown':
          e.preventDefault();
          if (canMoveDown()) handleDown();
          break;
        default:
          break;
      }
    };
    window.addEventListener('keydown', handleKeyDown);

    // cleanup listener on unmount
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [board, canMoveLeft, canMoveRight, canMoveUp, canMoveDown, hasZero, randomiseZero, isActive, isLost, isWon]);

  return (
    <>
      <Navbar />
      <div className="flex flex-col justify-left gap-8 mx-20">
        <h1 className="text-2xl font-bold mt-10">2048 Game</h1>

        { !isActive && !isLost && (
          <Button
            className="btn btn-primary px-4 py-2 w-40 rounded-xl"
            onClick={ startGame }
          >
            Start game
          </Button>
        ) }

        <div className="flex flex-col justify-center items-center">
          { isWon && (
            <h3 className="text-lg md:text-xl xl:text-2xl text-[#edc22e] font-bold mb-4">Game won! Well played!</h3>
          ) }
          { isLost && (
            <h3 className="text-lg md:text-xl xl:text-2xl text-red-500 font-bold mb-4">Game over! Play again?</h3>
          ) }

          { board.map((row, rowIndex) => {
            return (
              <div key={ `row-${ rowIndex }` } className="flex justify-center w-full transition-all duration-300">
                { row.map((val, colIndex) => {
                  return (
                    <div
                      key={ `row-${ colIndex }` }
                      className={ (() => {
                        switch (val) {
                          case 0:
                            return "bg-white flex items-center justify-center w-20 h-20 md:w-30 md:h-30 xl:w-40 xl:h-40 border-5 border-black";
                          case 2:
                            return "bg-[#eee4da] flex items-center justify-center w-20 h-20 md:w-30 md:h-30 xl:w-40 xl:h-40  border-5 border-black";
                          case 4:
                            return "bg-[#ede0c8] flex items-center justify-center w-20 h-20 md:w-30 md:h-30 xl:w-40 xl:h-40  border-5 border-black";
                          case 8:
                            return "bg-[#f2b179] flex items-center justify-center w-20 h-20 md:w-30 md:h-30 xl:w-40 xl:h-40  border-5 border-black";
                          case 16:
                            return "bg-[#f59563] flex items-center justify-center w-20 h-20 md:w-30 md:h-30 xl:w-40 xl:h-40  border-5 border-black";
                          case 32:
                            return "bg-[#f67c5f] flex items-center justify-center w-20 h-20 md:w-30 md:h-30 xl:w-40 xl:h-40  border-5 border-black";
                          case 64:
                            return "bg-[#f65e3b] flex items-center justify-center w-20 h-20 md:w-30 md:h-30 xl:w-40 xl:h-40  border-5 border-black";
                          case 128:
                            return "bg-[#edcf72] flex items-center justify-center w-20 h-20 md:w-30 md:h-30 xl:w-40 xl:h-40  border-5 border-black";
                          case 256:
                            return "bg-[#edcc61] flex items-center justify-center w-20 h-20 md:w-30 md:h-30 xl:w-40 xl:h-40  border-5 border-black";
                          case 512:
                            return "bg-[#edc850] flex items-center justify-center w-20 h-20 md:w-30 md:h-30 xl:w-40 xl:h-40  border-5 border-black";
                          case 1024:
                            return "bg-[#edc53f] flex items-center justify-center w-20 h-20 md:w-30 md:h-30 xl:w-40 xl:h-40 border-5 border-black";
                          case 2048:
                            return "bg-[#edc22e] flex items-center justify-center w-20 h-20 md:w-30 md:h-30 xl:w-40 xl:h-40  border-5 border-black";
                        }
                      })() }
                    >
                      <span className="text-md md:text-2xl xl:text-4xl font-black">{ val !== 0 && val }</span>
                    </div>
                  );
                }) }
              </div>
            );
          }) }
        </div>

        <p>Games won: { gamesCount }</p>

        <Button
          className="btn btn-primary px-4 py-2 w-40 rounded-xl mb-30"
          onClick={ restartGame }
        >
          Restart
        </Button>
      </div>
    </>
  );
}

export default Dashboard;