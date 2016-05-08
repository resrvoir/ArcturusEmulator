package com.eu.habbo.habbohotel.users.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.AbstractPet;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.users.Habbo;
import gnu.trove.TCollections;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Created on 28-8-2014 18:44.
 */
public class PetsComponent {

    private final TIntObjectMap<Pet> pets = TCollections.synchronizedMap(new TIntObjectHashMap<Pet>());

    public PetsComponent(Habbo habbo)
    {
        this.loadPets(habbo);
    }

    private void loadPets(Habbo habbo)
    {
        synchronized (this.pets)
        {
            try
            {
                PreparedStatement statement = Emulator.getDatabase().prepare("SELECT * FROM users_pets WHERE user_id = ? AND room_id = 0");
                statement.setInt(1, habbo.getHabboInfo().getId());
                ResultSet set = statement.executeQuery();

                while (set.next())
                {
                    this.pets.put(set.getInt("id"), new Pet(set));
                }

                set.close();
                statement.close();
                statement.getConnection().close();
            } catch (SQLException e)
            {
                Emulator.getLogging().logSQLException(e);
            }
        }
    }

    public Pet getPet(int id)
    {
        return this.pets.get(id);
    }

    public void addPet(Pet pet)
    {
        synchronized (this.pets)
        {
            this.pets.put(pet.getId(), pet);
        }
    }

    public void addPets(Set<Pet> pets)
    {
        synchronized (this.pets)
        {
            for (Pet p : pets)
            {
                this.pets.put(p.getId(), p);
            }
        }
    }

    public void removePet(AbstractPet pet)
    {
        synchronized (this.pets)
        {
            this.pets.remove(pet.getId());
        }
    }

    public TIntObjectMap<Pet> getPets()
    {
        return this.pets;
    }

    public int getPetsCount()
    {
        return this.pets.size();
    }

    public void dispose()
    {
        synchronized (this.pets)
        {
            TIntObjectIterator<Pet> petIterator = this.pets.iterator();

            for (int i = this.pets.size(); i-- > 0; )
            {
                try
                {
                    petIterator.advance();
                } catch (NoSuchElementException e)
                {
                    break;
                }
                if (petIterator.value().needsUpdate)
                    Emulator.getThreading().run(petIterator.value());
            }
        }
    }
}
