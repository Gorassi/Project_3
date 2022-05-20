package com.game.repository;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.game.entity.Race.*;


@RestController
public class MyController {

    @Autowired
    private PlayerRepository playerRepository;

    public Integer pageNumber = 0;
    public Integer pageSize = 3;

    List<Player> playerListAfterPaging;

    @GetMapping("/rest/players")
    public Iterable<Player> getFiteredUsers(
            @RequestParam(value = "name", required = false) String paramName,
            @RequestParam(value = "title", required = false) String paramTitle,
            @RequestParam(value = "race", required = false) Race paramRace,
            @RequestParam(value = "profession", required = false) Profession paramProfession,
            @RequestParam(value = "after", required = false) Long birthdayAfter,
            @RequestParam(value = "before", required = false) Long birthdayBefore,
            @RequestParam(value = "banned", required = false) Boolean paramBanned,
            @RequestParam(value = "minExperience", required = false) Integer paramMinExp,
            @RequestParam(value = "maxExperience", required = false) Integer paramMaxExp,
            @RequestParam(value = "minLevel", required = false) Integer paramMinLevel,
            @RequestParam(value = "maxLevel", required = false) Integer paramMaxLevel,
            @RequestParam(value = "pageNumber", required = false) Integer paramPageNumber,
            @RequestParam(value = "pageSize", required = false) Integer paramPageSize,
            @RequestParam(value = "order", required = false) PlayerOrder paramOrder
    ) {
        pageNumber = paramPageNumber == null ? 0 : paramPageNumber;
        pageSize = paramPageSize == null ? 3 : paramPageSize;

        List<Player> playerFilteredList = new ArrayList<>();

        for (Player player : playerRepository.findAll()) {
            boolean isName = paramName == null || player.getName().contains(paramName);
            boolean isTitle = paramTitle == null || player.getTitle().contains(paramTitle);
            boolean isRace = paramRace == null || player.getRace() == paramRace;
            boolean isProfession = paramProfession == null || player.getProfession() == paramProfession;
            boolean isAfter = (birthdayAfter == null) || (birthdayAfter <= player.getBirthday().getTime());
            boolean isBefore = (birthdayBefore == null) || (birthdayBefore >= player.getBirthday().getTime());
            boolean isNotBanned = (paramBanned == null) || (paramBanned.equals(player.isBanned()));
            boolean isMinExp = (paramMinExp == null) || (player.getExperience() >= paramMinExp);
            boolean isMaxExp = (paramMaxExp == null) || (player.getExperience() <= paramMaxExp);
            boolean isMaxLevel = (paramMaxLevel == null) || (player.getLevel() <= paramMaxLevel);
            boolean isMinLevel = (paramMinLevel == null) || (player.getLevel() >= paramMinLevel);

            if (isName && isTitle && isRace && isProfession && isNotBanned && isAfter && isBefore && isMinExp
                    && isMaxExp && isMinLevel && isMaxLevel) playerFilteredList.add(player);
        }

        // sort list by order
        if (paramOrder == null) paramOrder = PlayerOrder.ID;
        Comparator<Player> comparator = null;
        switch (paramOrder) {
            case ID:
                comparator = new IdComparator();
                break;
            case NAME:
                comparator = new NameComparator();
                break;
            case EXPERIENCE:
                comparator = new ExperienceComparator();
                break;
            case BIRTHDAY:
                comparator = new BirthdayComparator();
                break;
            case LEVEL:
                comparator = new LevelComparator();
                break;
        }

        Collections.sort(playerFilteredList, comparator);

        //limit players on page
        int countPlayersInList = playerFilteredList.size();
        int quantityPages = countPlayersInList / pageSize;
        int ostatok = countPlayersInList % pageSize;
        if (ostatok != 0) quantityPages++;
        int numberOfBlock = pageNumber * pageSize;
        playerListAfterPaging = new ArrayList<>();
        for (int i = numberOfBlock; i < numberOfBlock + pageSize; i++) {
            if (i < playerFilteredList.size()) playerListAfterPaging.add(playerFilteredList.get(i));
        }
        return playerListAfterPaging;
    }

    @GetMapping("/rest/players/count")
    public ResponseEntity<Integer> getCount(HttpServletRequest request,
                                            @RequestParam(value = "name", required = false) String paramName,
                                            @RequestParam(value = "title", required = false) String paramTitle,
                                            @RequestParam(value = "race", required = false) Race paramRace,
                                            @RequestParam(value = "profession", required = false) Profession paramProfession,
                                            @RequestParam(value = "after", required = false) Long birthdayAfter,
                                            @RequestParam(value = "before", required = false) Long birthdayBefore,
                                            @RequestParam(value = "banned", required = false) Boolean paramBanned,
                                            @RequestParam(value = "minExperience", required = false) Integer paramMinExp,
                                            @RequestParam(value = "maxExperience", required = false) Integer paramMaxExp,
                                            @RequestParam(value = "minLevel", required = false) Integer paramMinLevel,
                                            @RequestParam(value = "maxLevel", required = false) Integer paramMaxLevel,
                                            @RequestParam(value = "pageNumber", required = false) Integer paramPageNumber,
                                            @RequestParam(value = "pageSize", required = false) Integer paramPageSize,
                                            @RequestParam(value = "order", required = false) PlayerOrder paramOrder
    ) {

        List<Player> playerFilteredList = new ArrayList<>();
        for (Player player : playerRepository.findAll()) {
            boolean isName = paramName == null || player.getName().contains(paramName);
            boolean isTitle = paramTitle == null || player.getTitle().contains(paramTitle);
            boolean isRace = paramRace == null || player.getRace() == paramRace;
            boolean isProfession = paramProfession == null || player.getProfession() == paramProfession;
            boolean isAfter = (birthdayAfter == null) || (birthdayAfter <= player.getBirthday().getTime());
            boolean isBefore = (birthdayBefore == null) || (birthdayBefore >= player.getBirthday().getTime());
            boolean isNotBanned = (paramBanned == null) || (paramBanned.equals(player.isBanned()));
            boolean isMinExp = (paramMinExp == null) || (player.getExperience() >= paramMinExp);
            boolean isMaxExp = (paramMaxExp == null) || (player.getExperience() <= paramMaxExp);
            boolean isMaxLevel = (paramMaxLevel == null) || (player.getLevel() <= paramMaxLevel);
            boolean isMinLevel = (paramMinLevel == null) || (player.getLevel() >= paramMinLevel);

            if (isName && isTitle && isRace && isProfession && isNotBanned && isAfter && isBefore && isMinExp
                    && isMaxExp && isMinLevel && isMaxLevel) playerFilteredList.add(player);
        }
        return new ResponseEntity<>(playerFilteredList.size(), HttpStatus.OK);

    }


    @PostMapping("/rest/players/")
    public ResponseEntity<Player> createNewPlayer(@RequestBody Player newPlayer, HttpServletRequest request) {

        if (request.getQueryString() == null && newPlayer.getName() == null
                && newPlayer.getTitle() == null && newPlayer.getBirthday() == null
                && newPlayer.isBanned() == null && newPlayer.getRace() == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        boolean isInputDataIncorrect = false;

        isInputDataIncorrect = newPlayer == null || newPlayer.getName().equals("") || newPlayer.getTitle().equals("")
                || newPlayer.getBirthday() == null || newPlayer.getExperience() == null;
        if (newPlayer.getName().length() > 12) isInputDataIncorrect = true;
        if (newPlayer.getTitle().length() > 30) isInputDataIncorrect = true;
        if (newPlayer.getExperience() != null && newPlayer.getExperience() < 0) isInputDataIncorrect = true;
        if (newPlayer.getExperience() != null && newPlayer.getExperience() > 10000000) isInputDataIncorrect = true;
        if (newPlayer.getBirthday() != null && newPlayer.getBirthday().getTime() < 0) isInputDataIncorrect = true;

        if (!isInputDataIncorrect) {
            int experience = newPlayer.getExperience();
            int level = (int) ((Math.sqrt(2500D + 200D * experience) - 50) / 100);
            int untilNextlevel = 50 * (level + 1) * (level + 2) - experience;
            newPlayer.setLevel(level);
            newPlayer.setUntilNextLevel(untilNextlevel);
            Date minDate = null;
            Date maxDate = null;
            Date birthday = newPlayer.getBirthday();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                minDate = simpleDateFormat.parse("2000-01-01");
                maxDate = simpleDateFormat.parse("3000-12-31");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (birthday.before(minDate)) isInputDataIncorrect = true;
            if (birthday.after(maxDate)) isInputDataIncorrect = true;
            if (!isInputDataIncorrect) playerRepository.save(newPlayer);
        } else {
            System.out.println("Input dates incorrect ...");
        }
        return isInputDataIncorrect
                ? new ResponseEntity<>(HttpStatus.BAD_REQUEST)
                : new ResponseEntity<>(newPlayer, HttpStatus.OK);
    }

    @GetMapping("/rest/players/{id}")
    public ResponseEntity<Player> readPlayer(@PathVariable(name = "id") String id, HttpServletRequest request) {

        long result = getNumberFromUri(request.getRequestURI());

        if (result <= 0) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        final Optional<Player> optionalPlayer = playerRepository.findById(result);

        return optionalPlayer.isPresent()
                ? new ResponseEntity<>(optionalPlayer.get(), HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/rest/players/{id}")
    public ResponseEntity<Player> deletePlayerById(@PathVariable(value = "id") String strId, HttpServletRequest request) {

        long result = getNumberFromUri(request.getRequestURI());

        if (result <= 0) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        final Optional<Player> optionalPlayer = playerRepository.findById(result);

        if (optionalPlayer.isPresent()) {
            playerRepository.deleteById(result);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Modifying
    @Transactional
    @PostMapping("/rest/players/{id}")
    public ResponseEntity<Player> updatePlayerById(@PathVariable(name = "id") Long id, @RequestBody Player newPlayer, HttpServletRequest request) {

        long result = getNumberFromUri(request.getRequestURI());

        if (result <= 0) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Optional<Player> container = playerRepository.findById(result);

        if (!container.isPresent()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        Player oldPlayer = container.get();

        if (newPlayer.getName() == null && newPlayer.getTitle() == null) { // TODO: добавить проверку всех полей
            return ResponseEntity.ok(oldPlayer);
        }

        boolean isInputDataIncorrect = false;

        Integer experience = null;
        Integer level = null;
        Integer untilNextLevel = null;

        if (newPlayer.getName() == null) {
            newPlayer.setName(oldPlayer.getName());
        } else {
            if (newPlayer.getName().length() > 12) isInputDataIncorrect = true;
            if (newPlayer.getName().equals("")) newPlayer.setName(oldPlayer.getName());
        }

        if (newPlayer.getTitle() == null) {
            newPlayer.setTitle(oldPlayer.getTitle());
        } else {
            if (newPlayer.getTitle().length() > 30) isInputDataIncorrect = true;
            if (newPlayer.getTitle().equals("")) newPlayer.setTitle(oldPlayer.getTitle());
        }

        if (newPlayer.isBanned() == null) newPlayer.setBanned(false);

        if (newPlayer.getExperience() != null && newPlayer.getExperience() >= 0 && newPlayer.getExperience() <= 10000000) {
            experience = newPlayer.getExperience();
            level = (int) ((Math.sqrt(2500D + 200D * experience) - 50) / 100);
            untilNextLevel = 50 * (level + 1) * (level + 2) - experience;
            newPlayer.setLevel(level);
            newPlayer.setUntilNextLevel(untilNextLevel);
        } else {
            if (newPlayer.getExperience() != null) isInputDataIncorrect = true;
        }

        if (newPlayer.getExperience() == null) {
            experience = oldPlayer.getExperience();
            level = oldPlayer.getLevel();
            untilNextLevel = oldPlayer.getUntilNextLevel();
        }

        if (newPlayer.getBirthday() != null && newPlayer.getBirthday().getTime() < 0) isInputDataIncorrect = true;

        if (!isInputDataIncorrect && newPlayer.getBirthday() != null) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date minDate = null;
            Date maxDate = null;
            Date birthday = newPlayer.getBirthday();
            try {
                minDate = simpleDateFormat.parse("2000-01-01");
                maxDate = simpleDateFormat.parse("3000-12-31");
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (birthday.before(minDate)) {
                isInputDataIncorrect = true;
            }

            if (birthday.after(maxDate)) isInputDataIncorrect = true;
        }
        if (newPlayer.getBirthday() == null) newPlayer.setBirthday(oldPlayer.getBirthday());

        if (isInputDataIncorrect) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        if (newPlayer.getRace() == null) {
            newPlayer.setRace(oldPlayer.getRace());
        }

        if (newPlayer.getProfession() == null) {
            newPlayer.setProfession(oldPlayer.getProfession());
        }

        playerRepository.updateDatesById(id, newPlayer.getName(), newPlayer.getTitle(), newPlayer.getRace(),
                newPlayer.getProfession(), newPlayer.getBirthday(), newPlayer.isBanned(),
                experience, level, untilNextLevel);

        newPlayer.setId(id);
        newPlayer.setExperience(experience);
        newPlayer.setLevel(level);
        newPlayer.setUntilNextLevel(untilNextLevel);

        return new ResponseEntity<>(newPlayer, HttpStatus.OK);

    }

    public long getNumberFromUri(String uri) {
        long result = -1;
        String[] parts = uri.split("/rest/players/");
        try {
            result = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            System.out.println("Exception");
            result = -1;
        }
        return result;
    }
}
